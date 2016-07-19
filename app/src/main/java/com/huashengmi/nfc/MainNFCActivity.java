package com.huashengmi.nfc;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.huashengmi.nfc.databinding.ActivityMainNfcBinding;
import com.huashengmi.nfc.utils.NFCDataRelvoser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainNFCActivity extends AppCompatActivity {

    private static final DateFormat TIME_FORMAT = SimpleDateFormat
            .getDateTimeInstance();

    public static final String TAG = MainNFCActivity.class.getSimpleName();

    private View mRootView;
    private NfcAdapter mNfcAdapter;
    private TextView mResultTextView;

    private NFCDataRelvoser mNFCDataRelvoser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainNfcBinding nfcBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_nfc);
        mResultTextView  = nfcBinding.textView;
        mRootView = nfcBinding.getRoot();

        mNFCDataRelvoser = new NFCDataRelvoser();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Snackbar.make(mRootView, "该设备不支持NFC！", Snackbar.LENGTH_LONG).show();
            return;
        }

        //查看NFC是否开启
        if (!mNfcAdapter.isEnabled()) {
            Snackbar.make(mRootView, "请在系统设置中先启用NFC功能", Snackbar.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "action::" + getIntent().getAction());
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equalsIgnoreCase(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }




    private void processIntent(Intent intent) {
        //get the tag from intent
        StringBuffer sbuffer = new StringBuffer();
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            Log.i(TAG, "processIntent  tagFromIntent.getTechList()" + tech);
            sbuffer.append(tech + "\n");
        }

        //for example :MifareClassic
        boolean auth = false;
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        String metaInfo = "";
        try {
            mfc.connect();
            int type = mfc.getType();//obtain TAG's type
            int sectorCount = mfc.getSectorCount();//obtain TAG's sectorCounts
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型:" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间:" + mfc.getSize() + "B\n";
            //16扇区
            A : for (int i = 0; i < sectorCount; i++) {
                auth = mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector" + i + ":验证成功\n";
                    bCount = mfc.getBlockCountInSector(i);
                    bIndex = mfc.sectorToBlock(i);
                    //每个扇区有十块
                    for (int j = 0; j < bCount; j++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block" + bIndex + ":"
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                        if (j == 0) {
                            break A;
                        }
                    }
                } else {
                    //    metaInfo += "Sector" + i +":验证失败\n";
                }
            }
            Log.i(TAG, "NFC-->metaInfo--:" + metaInfo);
            Log.i(TAG, "result---->" + sbuffer.append(metaInfo));
            Log.i(TAG, "---->" + hexToString("0xe2"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String bytesToHexString(byte []src){
        StringBuilder stringBuilder = new StringBuilder("0x");
        if(src == null || src.length == 0){
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
           // Log.i(TAG, "bytesToHexString--->" + Arrays.toString(buffer));
            stringBuilder.append(buffer);
        }
        Log.i(TAG, "StringBuilder--->" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * Convenience method to convert a byte array to a hex string.
     *
     * @param data the byte[] to convert
     * @return String the converted byte[]
     */

    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]).toUpperCase());
            buf.append(" ");
        }
        return (buf.toString());
    }

    /**
     * method to convert a byte to a hex string.
     *
     * @param data the byte to convert
     * @return String the converted byte
     */
    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convenience method to convert an int to a hex char.
     *
     * @param i the int to convert
     * @return char the converted char
     */
    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    /**
     * 16进制转String
     *
     * @author YOLANDA
     * @param bytes
     * @return
     */

    private String hexString = "0123456789ABCDEF";

    public String hexToString(String hexStr) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexStr.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < hexStr.length(); i += 2) {
            try {
                baos.write((hexString.indexOf(hexStr.charAt(i)) << 4 | hexString.indexOf(hexStr.charAt(i + 1))));
            } catch (StringIndexOutOfBoundsException e) {
                return "非16进制数据";
            }
        }
        return new String(baos.toByteArray());
    }

}
