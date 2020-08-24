package com.sty.dj.barcodescaledemo;

import AclasLSToolSdk.AclasLSTool;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sty.dj.barcodescaledemo.filechooser.FileChooserDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/*
 * steps:
 * 1 Init(): init page and tools = new AclasLSTool()
 * 2 send plu file to scaler sendTxt() /send hotkey file to scaler sendTxtHot()
 * 3 UnInit()
 *
 */
public class MainActivity extends Activity {
    final	private String tag			= "AclasLSToolDemo";
    private AclasLSTool tools 	= null;

    //private String strLngCode = "UTF-16";
    private String strLngCode = "Unicode";

    private Button btnSend		= null;
    private	Button btnInit		= null;
    private	Button btnSendSyn	= null;
    private	Button btnUnInit	= null;
    private Timer timer 		= null;
    private int	iBtnTimeout		= 100000;

    private	Button btnSendHot	= null;
    private	Button btnSendHotByArray	= null;


    private Button btnSelectPlu	= null;
    private Button btnSelectHot	= null;
    FileChooserDialog dialog = null;
    private int		iBtnIndex = 0;//0: select PLU file; 1:select Hotkey file


    private	Button btnReadPlu	= null;
    private	Button btnReadHotKey	= null;
    private String strReadPLUFile		= null;
    private String strReadHotKeyFile = null;

    private TextView tvPluCnt	= null;
    private TextView    tvHkCnt		= null;
    private	int 		iPluCnt     = 0;
    private int			iHkCnt      = 0;
    private ArrayList<String> listString	= new ArrayList<String>();

    private TextView	tv_info = null;
    private CheckBox cbLink66 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init();
        InitSelectFold();
        Log.d(tag, "sdk init complete");
        Log.d(tag, "外置存储： " + Environment.getExternalStorageDirectory().getAbsolutePath());

    }


    private String strInit;
    private String strSuccess;
    private String strFailed;
    private String strSendPlu;
    private String strComplete;
    private String strSendHk;
    private String strReadPLU;
    private String strReadHk;

    private void InitString(){
        strInit	= getResources().getString(R.string.btn_init);
        strSuccess	= getResources().getString(R.string.str_success);
        strFailed	= getResources().getString(R.string.str_failed);
        strSendPlu	= getResources().getString(R.string.btn_send_plu);
        strComplete	= getResources().getString(R.string.str_complete);
        strSendHk	= getResources().getString(R.string.btn_send_hk);
        strReadPLU	= getResources().getString(R.string.btn_read_plu);
        strReadHk	= getResources().getString(R.string.btn_read_hk);
    }

    Handler gui_show = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch(msg.what)
            {
                case 0:  //初始化是否成功 1：成功   0：失败
                    if(msg.arg1==1){
                        btnSend.setEnabled(true);
                        btnInit.setEnabled(false);
                        btnUnInit.setEnabled(true);

                        btnReadPlu.setEnabled(true);
                        btnReadHotKey.setEnabled(true);

                        btnSendHot.setEnabled(true);
                        btnSendHotByArray.setEnabled(true);
                    }
                    Toast.makeText(MainActivity.this,strInit+(msg.arg1==1?strSuccess:strFailed),Toast.LENGTH_SHORT).show();
                    break;
                case 1:  //发送数据是否成功 arg1:是否完成（1：完成   0：未完成）   arg2:是否成功（1：成功   0：失败）
                    String info = "";
                    if(msg.arg1==0){
                        info = strSendPlu+(msg.arg2==1?strSuccess:strFailed)+" "+msg.obj;
                    }else{
                        info = strSendPlu+ strComplete+" "+msg.obj;
                    }
                    Toast.makeText(MainActivity.this,info,Toast.LENGTH_SHORT).show();
                    OperateTimeout(false);
                    tv_info.setText(info);
                    break;
                case 2:  //onError

                    Toast.makeText(MainActivity.this,"On error "+msg.obj,Toast.LENGTH_SHORT).show();
//                	OperateTimeout(true);
                    break;
                case 3:

                    Toast.makeText(MainActivity.this,"On send txt syn "+(msg.arg1==0?"sucess":"failed"),Toast.LENGTH_SHORT).show();
                case 4://time out
                    OperateTimeout(false);
                    break;
                case 5: // onSendHot  1:成功  0：失败
                    Toast.makeText(MainActivity.this,strSendHk+((msg.arg1==1?strSuccess:strFailed)+",table:")+msg.obj,Toast.LENGTH_SHORT).show();

                    OperateTimeout(false);
                    break;
                case 6:  //onRecvPluData
                    if(msg.arg1==0){  //iCode==1
                        OperateTimeout(false);
                        String str 	= strReadPLU+String.valueOf(iPluCnt)+strSuccess;
                        tvPluCnt.setText(str);
                        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
                    }else {
                        if(msg.arg2==1){ //iCode==0
                            iPluCnt++;
                            if(iPluCnt%10==0){
                                tvPluCnt.setText(String.valueOf(iPluCnt));
                            }
                        }else{ //iCode==-1

                        }
                    }
                    break;
                case 7: //onRecvHotKeyData
                    OperateTimeout(false);
                    tvHkCnt.setText(strReadHk+String.valueOf(msg.arg1)+strSuccess);
                    Toast.makeText(MainActivity.this,strReadHk+String.valueOf(msg.arg1)+strSuccess,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*
     * init page and new AclasLSTool()
     */
    private void Init() {
        InitString();
        btnUnInit	= (Button)findViewById(R.id.btn_uninit);
//		btnUnInit.setEnabled(false);
        btnUnInit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(tools!=null){
                    new Thread(){
                        public void run(){

                            tools.UnInit();
                            tools	= null;
                        }
                    }.start();
//					tools.UnInit();
//					tools	= null;
                    btnInit.setEnabled(true);
                    btnSend.setEnabled(false);
                    btnSendHot.setEnabled(false);
                    btnSendHotByArray.setEnabled(false);

                    btnReadPlu.setEnabled(false);
                    btnReadHotKey.setEnabled(false);
                }
            }
        });

        btnInit	= (Button)findViewById(R.id.btn_init);
        btnInit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                btnSendSyn.setEnabled(false);

                tools = new AclasLSTool();

                tools.setLogSwitch(true);
                String strVer = tools.getVersion();
                Log.d(tag, "Version:"+strVer);

                Spinner spLang		= (Spinner)findViewById(R.id.lang_sp_type);
                tools.setStringCode(spLang.getSelectedItemPosition());

                EditText etIp = (EditText)findViewById(R.id.etIp);
                String ipString = etIp.getText().toString();


                tools.Init(ipString, new AclasLSTool.AclasLsToolListener(){
                    /**
                     * 初始化通知
                     * @param bFlag 成功或失败 true false
                     * @param arg1
                     */
                    @Override
                    public void onInit(boolean bFlag, String arg1) {
                        // TODO Auto-generated method stub
                        Log.d(tag, "onInit:"+bFlag);

                        Message msg_paperstatus = gui_show.obtainMessage();
                        msg_paperstatus.what = 0;
                        msg_paperstatus.arg1 = bFlag?1:0;  //1:成功 2：失败
                        gui_show.sendMessage(msg_paperstatus);
                    }

                    /**
                     * 发送PLU数据通知
                     * @param bComplete  发送是否完成 true false
                     * @param bFlag 发送成功失败标志位  true false
                     * @param info  内容为：{Complete Success: Num  Failed: Num}
                     */
                    @Override
                    public void onSendData(boolean bComplete,boolean bFlag,String info) {

                        Log.d(tag, "onSendData:"+bFlag+" PLU NO:"+info);

                        Message msg_paperstatus = gui_show.obtainMessage();
                        msg_paperstatus.what = 1;
                        msg_paperstatus.arg1 = bComplete?1:0;
                        msg_paperstatus.arg2 = bFlag?1:0;
                        msg_paperstatus.obj	 = new String(info);
                        gui_show.sendMessage(msg_paperstatus);
                    }

                    /**
                     * 其它出错信息
                     * @param info
                     */
                    @Override
                    public void onError(String info) {

                        Log.d(tag, "onError:"+info);

                        Message msg_paperstatus = gui_show.obtainMessage();
                        msg_paperstatus.what = 2;
                        msg_paperstatus.obj	 = new String(info);
                        gui_show.sendMessage(msg_paperstatus);
                    }

                    /**
                     * 发送热键数据通知
                     * @param bFlag 成功失败标志位 true false
                     * @param info 表号：共224，分4张表发送，每张64
                     */
                    @Override
                    public void onSendHotKey(boolean bFlag,String info){

                        Log.d(tag, "onSendHot:"+bFlag+" table:"+info);

                        Message msg_paperstatus = gui_show.obtainMessage();
                        msg_paperstatus.what = 5;
                        msg_paperstatus.arg1 = bFlag?1:0;
                        msg_paperstatus.obj	 = new String(info);
                        gui_show.sendMessage(msg_paperstatus);
                    }

                    /**
                     * receive on plu data when plu is not null
                     * @param plu: data
                     * @param iCode: 1读取plu结束；0还有下一条plu数据；-1读取到重复的plu
                     */
                    public void onRecvPluData(AclasLSTool.St_Plu_Data plu, int iCode)
                    {
                        if(iCode==1)
                        {
                            saveDataToTxt(strReadPLUFile, listString);
                            strReadPLUFile	= null;
                            Log.d(tag, "onRecvPluData:complete");


                            Message msg = gui_show.obtainMessage();
                            msg.what = 6;
                            msg.arg1 = 0;
                            gui_show.sendMessage(msg);
//							OperateTimeout(true);
                        }else if(iCode==0){

                            if(strReadPLUFile==null){
                                strReadPLUFile	= "ReadPLU"+getDateString()+".txt";
                                listString.add(plu.getHeader()+"\r"+"\n");
                            }
                            listString.add(plu.toString()+"\r"+"\n");

                            Message msg = gui_show.obtainMessage();
                            msg.what = 6;
                            msg.arg1 = 1;
                            msg.arg2 = 1;
                            gui_show.sendMessage(msg);

                            Log.d(tag, "No:"+plu.iPluNo+" price:"+plu.dPrice+" name:"+plu.strName+" price:"+plu.dPrice+" Unit:"+plu.ucUnit+" bar:"+plu.ucBarcodeType+" label:"+plu.ucLabelNo+" dep:"+plu.ucDepart
                                    + " pkgR:"+plu.ucPkgRange+" pkgT:"+plu.ucPkgType+" wei:"+plu.dPkgWeight+" Code:"+plu.strPluCode+" intCode:"+plu.iPluCode);
                        }else if(iCode==-1){

                            Message msg = gui_show.obtainMessage();
                            msg.what = 6;
                            msg.arg1 = 1;
                            msg.arg2 = 0;
                            gui_show.sendMessage(msg);
                            Log.d(tag, "Same No:"+plu.iPluNo+" price:"+plu.dPrice+" name:"+plu.strName+" price:"+plu.dPrice+" Unit:"+plu.ucUnit+" bar:"+plu.ucBarcodeType+" label:"+plu.ucLabelNo+" dep:"+plu.ucDepart
                                    + " pkgR:"+plu.ucPkgRange+" pkgT:"+plu.ucPkgType+" wei:"+plu.dPkgWeight);
                        }
                    }

                    /**
                     * receive hot keys
                     * @param keys: hot key data -> 所有热键
                     */
                    public void onRecvHotKeyData(ArrayList<AclasLSTool.St_HotKey> keys)
                    {
                        Log.d(tag, "onRecvHotKeyData size:"+keys.size());

                        String string = "";

                        if(strReadHotKeyFile==null){
                            strReadHotKeyFile	= "ReadHK"+getDateString()+".txt";
                        }
                        listString.clear();
                        for(int i=0;i<keys.size();i++)
                        {
                            AclasLSTool.St_HotKey key = keys.get(i);
                            string += " "+String.valueOf(key.iIndex)+" "+String.valueOf(key.iPluNo);
                            if((i+1)%10==0)
                            {
                                string += "\n";
                            }
                            listString.add(key.toString()+"\r"+"\n");
                            //saveDataToTxt(strReadHotKeyFile, key.toString()+"\r"+"\n");

                        }
                        saveDataToTxt(strReadHotKeyFile, listString);
                        strReadHotKeyFile = null;
                        //Log.d(tag, string);
                        Message msg = gui_show.obtainMessage();
                        msg.what = 7;
                        msg.arg1	= keys.size();
                        gui_show.sendMessage(msg);

//						OperateTimeout(true);
                    }
                });

                saveEditTextInfo(0, ipString);
            }
        });

        btnSend	= (Button)findViewById(R.id.btn_send);
//		btnSend.setEnabled(false);

        //文件路径为空 并且没有勾选Link66文件  将发送plu array
        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //sendData();
                OperateTimeout(true);

                tv_info.setText("Begin to send plu-----------");
                sendTxt();

            }
        });

        btnSendSyn = (Button)findViewById(R.id.btn_sendSyn);
        btnSendSyn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                btnInit.setEnabled(false);

                EditText etPath = (EditText)findViewById(R.id.etPath);
                String strPath	= etPath.getText().toString();
                EditText etIp = (EditText)findViewById(R.id.etIp);
                String ipString = etIp.getText().toString();
                if(tools==null){
                    tools = new AclasLSTool();
                }
                int iRet = tools.sendPluTxtSyn(ipString, strPath);


                Message msg_paperstatus = gui_show.obtainMessage();
                msg_paperstatus.what = 3;
                msg_paperstatus.arg1	 = iRet;
                gui_show.sendMessage(msg_paperstatus);
            }
        });



        btnSendHot	= (Button)findViewById(R.id.btn_sendHot);
        btnSendHot.setEnabled(false);
        btnSendHot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //sendData();
                OperateTimeout(true);
                sendTxtHot();
            }
        });

        btnSendHotByArray = findViewById(R.id.btn_sendHot_by_array);
        btnSendHotByArray.setEnabled(false);
        btnSendHotByArray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperateTimeout(true);
                sendTxtHotByArray();
            }
        });


        btnSelectPlu	= (Button)findViewById(R.id.btn_selectPlu);
        btnSelectPlu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //sendData();

                iBtnIndex	= 0;
                dialog.show();
            }
        });

        btnSelectHot	= (Button)findViewById(R.id.btn_selectHot);
        btnSelectHot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //sendData();
                iBtnIndex	= 1;
                dialog.show();
            }
        });


        btnReadPlu	= (Button)findViewById(R.id.btn_readplu);
        btnReadPlu.setEnabled(false);
        btnReadPlu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                OperateTimeout(true);
                iPluCnt	= 0;
                listString.clear();
                tools.readAllPluData();

            }
        });

        btnReadHotKey	= (Button)findViewById(R.id.btn_readhotkey);
        btnReadHotKey.setEnabled(false);
        btnReadHotKey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //sendData();

                OperateTimeout(true);
                iHkCnt	= 0;
                listString.clear();
                tools.readAllHotKeys();

            }
        });

        tvPluCnt	= (TextView)findViewById(R.id.tvplucnt);

        tvHkCnt	= (TextView)findViewById(R.id.tvhkcnt);

        String readString	= readEditTextInfo(3);
        if(readString!=null){
            EditText etIp = (EditText)findViewById(R.id.etIp);
            etIp.setText(readEditTextInfo(0));
            EditText etPathPlu = (EditText)findViewById(R.id.etPath);
            etPathPlu.setText(readEditTextInfo(1));
            EditText etPathHot = (EditText)findViewById(R.id.etPathHot);
            etPathHot.setText(readEditTextInfo(2));
        }
        tv_info		= (TextView)findViewById(R.id.tv_info);

        cbLink66	= (CheckBox)findViewById(R.id.cb_pc);


        btnInit.setFocusable(true);
        btnInit.setFocusableInTouchMode(true);
        btnInit.requestFocus();
    }

    /*
     * send plu file to scaler
     */
    public void sendTxt() {
        //tools.sendPluFile("/storage/emulated/0/Download/plu.txt");
        //tools.sendPluFile(strPath);


        //------------
        EditText etPath = (EditText)findViewById(R.id.etPath);
        String strPath	= etPath.getText().toString();
        if(strPath.length()>0){
            if(cbLink66.isChecked()){
                tools.sendPluPCFile(strPath);
            }else{
                tools.sendPluFile(strPath);
            }
            saveEditTextInfo(1, strPath);
        }else{
            AclasLSTool.St_Plu_Data	data =  tools.new St_Plu_Data();
            //生产日期 精确到分钟
            data.dateProduced	= new Date();
            //包装重量
            data.dPkgWeight		= 1f;
            //单价
            data.dPrice			= 2.0f;
            //保鲜天数
            data.iFreshDays		= 280;
            //包装天数
            data.iPackageDays	= 0;
            //包装小时
            data.iPackageHours	= 0;
            //货号 1-99999999更多的位数请用strPluCode
            data.iPluCode		= 2400028;
            //生鲜码 1~999999
            data.iPluNo			= 900001;
            //保质天数
            data.iValidDays		= 288;
            //商品名 最多四十个字符
            data.strName		= "卫龙魔芋素毛肚";
            //条码类型 0~250
            data.ucBarcodeType	= (byte)150;
            //部门号 1-99
            data.ucDepart		= 24;
            data.ucFlag1		= (byte)0x7c;
            data.ucFlag2		= (byte)0xff;
            data.ucFlag3		= (byte)0xf0;
            //保鲜日期时间基点 0:当前日期 1:生产日期 2:包装日期 3:不計算
            data.ucFreshnessDateFrom	= 0;
            //标签号 1~32
            data.ucLabelNo		= 1;
            //包装日期时间基点 0:当前日期 1:生产日期
            data.ucPackageDateFrom		= 0;
            //包装误差 0-99
            data.ucPkgRange		= 1;
            //包装类型 0:正常 1:定重 2:定价 3:定重定价 4:拼盘
            data.ucPkgType		= 0;
            //生产日期时间基点 0:当前日期 1:生产日期
            data.ucProducedDateRule		= 0;
            //单位； 0x04: kg 0x0a: pcs_kg 0x07 500g
            data.ucUnit			= 0x04;
            //保质日期时间基点 0:当前日期 1:生产日期 2:包装日期 3:不計算
            data.ucValidDateFrom		= 0;
            //最多16位数字
            data.strPluCode		= "2400028012345";
            ArrayList<AclasLSTool.St_Plu_Data> list  = new ArrayList<AclasLSTool.St_Plu_Data>();
            list.add(data);
            tools.sendPluArray(list);
        }
        //---------------------------



    }

    /*
     * send hotkey file to scaler
     */
    public void sendTxtHot() {
        //tools.sendPluFile("/storage/emulated/0/Download/plu.txt");
        EditText etPath = (EditText)findViewById(R.id.etPathHot);
        String strPath	= etPath.getText().toString();
        tools.sendHotKeyFile(strPath);

        saveEditTextInfo(2, strPath);


    }

    /**
     * send hotkey Array to scaler
     */
    private void sendTxtHotByArray() {
        ArrayList<AclasLSTool.St_HotKey> hotKeyList = new ArrayList<>();
        AclasLSTool.St_HotKey hotKey = tools.new St_HotKey();
        hotKey.iIndex = 3;
        hotKey.iPluNo = 11;  //上海青
        hotKeyList.add(hotKey);

        AclasLSTool.St_HotKey hotKey2 = tools.new St_HotKey();
        hotKey2.iIndex = 14;
        hotKey2.iPluNo = 12; //菠菜
        hotKeyList.add(hotKey2);

        AclasLSTool.St_HotKey hotKey3 = tools.new St_HotKey();
        hotKey3.iIndex = 126;
        hotKey3.iPluNo = 13; //生菜
        hotKeyList.add(hotKey3);

        tools.sendHotKeys(hotKeyList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    private void OperateTimeout(boolean bFlag) {
        Log.d(tag, "OperateTimeout:"+bFlag);
        if(bFlag){
            btnSend.setEnabled(false);
            btnSendHot.setEnabled(false);
            btnSendHotByArray.setEnabled(false);
            btnReadPlu.setEnabled(false);
            btnReadHotKey.setEnabled(false);
            if(timer==null){
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Message msg = gui_show.obtainMessage();
                    msg.what	= 4;
                    gui_show.sendMessage(msg);
                }
            }, iBtnTimeout);
        }else{
            btnSend.setEnabled(true);
            btnSendHot.setEnabled(true);
            btnSendHotByArray.setEnabled(true);
            btnReadPlu.setEnabled(true);
            btnReadHotKey.setEnabled(true);
            if(timer!=null){
                timer.cancel();
                timer = null;
            }
        }
    }

    private void InitSelectFold(){
        File fDownFold 	= Environment.getExternalStorageDirectory();
        Log.d(tag, "Path:"+Environment.getExternalStorageDirectory().toString()+" down:"+Environment.getDownloadCacheDirectory().toString());
        dialog = new FileChooserDialog(this,fDownFold.getAbsolutePath());

        dialog.setFilter(".*TXT;.*txt");
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            @Override
            public void onFileSelected(Dialog source, File folder, String name) {}
            @Override
            public void onFileSelected(Dialog source, File file) {

                if(iBtnIndex==0){
                    EditText etPath = (EditText)findViewById(R.id.etPath);
                    etPath.setText(file.getAbsolutePath());
                }else{
                    EditText etPath = (EditText)findViewById(R.id.etPathHot);
                    etPath.setText(file.getAbsolutePath());
                }
//				source.hide();
                source.dismiss();
            }
        });
    }

    private String stringToUniconde(String str){
        StringBuffer sb = new StringBuffer();
        char[] c	= str.toCharArray();
        for(int i=0;i<c.length;i++){
            sb.append("\\u"+Integer.toHexString(c[i]));
        }
        return sb.toString();
    }
    private void saveDataToTxt(String strFileName,ArrayList<String> list){
        if(strFileName!=null&&!strFileName.isEmpty()&&!list.isEmpty()){
            try {

//				String strData = new String(strDataIn.getBytes(),"UTF-16");
//				String strData = stringToUniconde(strDataIn);
                File fDown 			= Environment.getExternalStorageDirectory();
                String strDown		= fDown.getAbsolutePath();
//		    	Log.d(tag, "Download path:"+strDown);
                String aclaString	= strDown+"/Download/Aclas/";
                final String LOG_FILE = strDown+"/Download/Aclas/"+strFileName;

                try {
                    File  fold = new File(aclaString);
                    if(!fold.exists()){
                        fold.mkdir();
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                try {

//					Log.d(tag, "write file path:"+LOG_FILE);
                    File file = new File(LOG_FILE);
                    if(!file.exists()){
                        file.createNewFile();
                        String cmd = "chmod 777 "+file.getAbsolutePath();
                        Runtime runtime = Runtime.getRuntime();
                        java.lang.Process proc = runtime.exec(cmd);
                    }
                    BufferedWriter w  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true),"UTF-16"));
                    for (int i = 0; i < list.size(); i++) {
                        String strData	= list.get(i);
                        w.write(strData);
                    }
                    w.flush();
                    w.close();
//					FileWriter writer = new FileWriter(file, true);
//					writer.write(strData, 0,strData.length());
//					writer.close();
                } catch (Exception e) {

                    // TODO: handle exception
                    Log.e(tag, "saveDataToTxt error:"+e.toString());
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /*
     * save 0 ip,
     *      1 plu file path,
     *      2 hotkey file path
     */
    private void saveEditTextInfo(int iType,String strIn) {
        if(strIn.length()>0){
            File fDown 			= Environment.getExternalStorageDirectory();
            String strDown		= fDown.getAbsolutePath();
            Log.d(tag, "Download path:"+strDown);
            String aclaString	= strDown+"/Download/Aclas/";
            final String LOG_FILE = strDown+"/Download/Aclas/settings.txt";

            try {
                File  fold = new File(aclaString);
                if(!fold.exists()){
                    fold.mkdir();
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            try {

                Log.d(tag, "write file path:"+LOG_FILE);
                File file = new File(LOG_FILE);
                String preString	= "";
                if(file.exists()){
                    preString	= readEditTextInfo(3);
                    file.delete();
                }

                String[] list = null;
                if(preString.length()>0){
                    list = preString.split(";");
                }
                String strOut 	= "";
                if(list!=null&&iType<list.length){
                    list[iType]		= strIn;
                    for (int i = 0; i < list.length; i++) {
                        strOut  += list[i];
                        if(i!=list.length-1){
                            strOut += ";";
                        }
                    }
                }else{
                    switch (iType) {
                        case 0:
                            strOut	= strIn+";/mnt/internal_sd/scale/plu4.txt;/mnt/internal_sd/scale/66hk.txt";
                            break;
                        case 1:
                            strOut	= "192.168.1.87;"+strIn+";/mnt/internal_sd/scale/66hk.txt";
                            break;
                        case 2:
                            strOut	= "192.168.1.87;/mnt/internal_sd/scale/plu4.txt;"+strIn;
                            break;

                        default:
                            break;
                    }
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file, true);
                writer.write(strOut, 0,strOut.length());
                writer.close();
            } catch (Exception e) {

                // TODO: handle exception
                Log.e(tag, "saveComAddr error:"+e.toString());
            }
        }
    }

    /*
     * read 0 ip,
     * 		1 plu file path,
     * 		2 hotkey file path,
     *      3 all
     */
    private String readEditTextInfo(int iType) {

        File fDown 			= Environment.getExternalStorageDirectory();
        String strDown		= fDown.getAbsolutePath();
        Log.d(tag, "Download path:"+strDown);
        final String LOG_FILE = strDown+"/scale/Aclas/settings.txt";
        //String LOG_FILE = "/storage/sdcard0/Download/settings.txt";//"/storage/emulate.0/Download/setting.txt"
        Log.d(tag, "read file path:"+LOG_FILE);
        String strOut = null;
        File file = new File(LOG_FILE);
        if(file.exists()){

            try {
                char[] buffer = new char[1024];

                FileReader	reader = new FileReader(file);
                int iCnt = reader.read(buffer);
                if( -1!=iCnt ){
                    strOut = String.valueOf(buffer, 0, iCnt);
                }

                reader.close();
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(tag, "readComAddr error:"+e.toString());
            }

        }
        if(strOut!=null){
            String[] list	= strOut.split(";");
            if(list.length>1&&list.length>iType){

                switch (iType) {
                    case 0:
                        strOut	= list[0];
                        break;
                    case 1:
                        strOut	= list[1];
                        break;
                    case 2:
                        strOut	= list[2];
                        break;

                    default:
                        break;
                }
            }
        }
        return strOut;
    }

    private String getDateString(){

        String FORM_STRING = "yyyyMMddHHmmss";
        SimpleDateFormat date = new SimpleDateFormat(FORM_STRING, Locale.getDefault());
        return date.format(new java.util.Date());
    }
}