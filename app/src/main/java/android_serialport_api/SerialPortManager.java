package android_serialport_api;

import android.fpi.MtGpio;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.zyapi.CommonApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortManager {

    private static SerialPortManager mSerialPortManager = new SerialPortManager();

    public static final int DEVTYPE_UART = 0;
    public static final int DEVTYPE_SPI = 1;
    public static final int DEVTYPE_USB = 2;

    public static final int DEV_UART_3G_5O = 0x01;    //Old 4.4.4
    public static final int DEV_UART_3G_5N = 0x02;    //Android
    public static final int DEV_SPI_3G_7 = 0x03;
    public static final int DEV_UART_3G_6 = 0x04;        //ACCESS
    public static final int DEV_UART_4G_5 = 0x05;
    public static final int DEV_UART_4G_T = 0x06;
    public static final int DEV_UART_4G_6 = 0x07;
    public static final int DEV_USB_4G_7 = 0x08;
    public static final int DEV_USB_4G_8 = 0x09;
    public static final int DEV_UART_4G_7 = 0x10;
    public static final int DEV_UART_3G_ACCESS = 0x11;
    public static final int DEV_UART_HF_A5 = 0x12;

    private static final int BAUDRATE = 460800;
    private static final int Speed = 2000 * 1000;
    private static final int Mode = 1;

    private int mDeviceType = 0;

    private SerialPort mSerialPort = null;
    private boolean isOpen;
    private boolean firstOpen = false;

    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private byte[] mBuffer = new byte[128 * 1024];
    private int mCurrentSize = 0;
    private Looper looper;
    private HandlerThread ht;
    private ReadThread mReadThread;

    private boolean bCancel = false;
    private AsyncFingerprint asyncFP = null;

    public AsyncFingerprint getNewAsyncFingerprint() {
        if (!isOpen) {
            try {
                openSerialPort();
                isOpen = true;
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Cancel(false);
            asyncFP = new AsyncFingerprint(looper);
            asyncFP.Cancel(false);
            Log.i("xpb", "Open Serial");
            return asyncFP;
        }
        return asyncFP;
        //return new AsyncFingerprint(looper);
    }

    public SerialPortManager() {
    }


    public static SerialPortManager getInstance() {
        return mSerialPortManager;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
    }

    private void createWorkThread() {
        ht = new HandlerThread("workerThread");
        ht.start();
        looper = ht.getLooper();
    }

    public void openSerialPort() throws SecurityException, IOException,
            InvalidParameterException {
        if (mSerialPort == null) {
            /* Open the serial port */
            mSerialPort = new SerialPort();

            mDeviceType = getDeviceType();
            if (mDeviceType == DEV_SPI_3G_7) {
                Log.i("xpb", "SPI Mode");
                mSerialPort.OpenDevice(new File(GetUartPath()), Speed, Mode, DEVTYPE_SPI);
                setUpGpio();
                SystemClock.sleep(500);
            } else {
                Log.i("xpb", "UART Mode 4G");
                setUpGpio();
                SystemClock.sleep(500);
                mSerialPort.OpenDevice(new File(GetUartPath()), BAUDRATE, 0, DEVTYPE_UART);
            }

            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            if ((mDeviceType == DEV_UART_3G_5O) ||
                    (mDeviceType == DEV_UART_3G_5N) ||
                    (mDeviceType == DEV_UART_3G_6) ||
                    (mDeviceType == DEV_UART_4G_5) ||
                    (mDeviceType == DEV_UART_4G_T) ||
                    (mDeviceType == DEV_UART_4G_7) ||
                    (mDeviceType == DEV_UART_3G_ACCESS) ||
                    (mDeviceType == DEV_UART_HF_A5) ||
                    (mDeviceType == DEV_UART_4G_6)) {
                mReadThread = new ReadThread();
                mReadThread.start();
            }
            isOpen = true;
            createWorkThread();
            firstOpen = true;
        }
    }

    public void PowerControl(boolean sw) {
        if (sw) {
            try {
                setUpGpio();
            } catch (IOException e) {
            }
        } else {
            try {
                setDownGpio();
            } catch (IOException e) {
            }
        }
    }

    public void closeSerialPort() {
        Cancel(true);
        asyncFP.Cancel(true);
        if (ht != null) {
            ht.quit();
        }
        ht = null;
        if (mDeviceType == DEV_SPI_3G_7) {
            SystemClock.sleep(1000);
        } else {
            SystemClock.sleep(200);
        }

        if (mReadThread != null)
            mReadThread.interrupt();
        mReadThread = null;
        try {
            setDownGpio();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (mSerialPort != null) {
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen = false;
        mCurrentSize = 0;

        Log.i("xpb", "Close Serial");
    }

    private boolean checkCmdTag(byte[] data) {
        for (int i = 0; i < data.length - 4; ) {
            if (((byte) data[i] == (byte) (0xEF)) &&
                    ((byte) data[i + 1] == (byte) (0x01)) &&
                    ((byte) data[i + 2] == (byte) (0xFF)) &&
                    ((byte) data[i + 3] == (byte) (0xFF))
                    ) {
                return true;
            }
        }
        return false;
    }

    protected /*synchronized*/ int read(byte buffer[], int size, int waittime) {
        if (bCancel) {
            Log.i("xpb", "Cancel=" + String.valueOf(bCancel));
            return 0;
        }

        if (mDeviceType != DEV_SPI_3G_7) {
            int time = 4000;
            int sleepTime = 50;
            int length = time / sleepTime;
            boolean shutDown = false;
            int[] readDataLength = new int[3];
            for (int i = 0; i < length; i++) {
                if (mCurrentSize == 0) {
                    SystemClock.sleep(sleepTime);
                    continue;
                } else {
                    break;
                }
            }

            if (mCurrentSize > 0) {
                while (!shutDown) {
                    SystemClock.sleep(sleepTime);
                    readDataLength[0] = readDataLength[1];
                    readDataLength[1] = readDataLength[2];
                    readDataLength[2] = mCurrentSize;
                    Log.i("whw", "read2    mCurrentSize=" + mCurrentSize);
                    if (readDataLength[0] == readDataLength[1]
                            && readDataLength[1] == readDataLength[2]) {
                        shutDown = true;
                    }
                }
                if (mCurrentSize <= buffer.length) {
                    System.arraycopy(mBuffer, 0, buffer, 0, mCurrentSize);
                }
            }
            return mCurrentSize;
        } else {
            mCurrentSize = 0;
            SystemClock.sleep(waittime);

            byte[] revbuf = new byte[150];
            int n = (size / 139 + 1);
            for (int t = 0; t < n; t++) {
                if (bCancel) {
                    Log.i("xpb", "Cancel=" + String.valueOf(bCancel));
                    return 0;
                }
                try {
                    SystemClock.sleep(2);
                    mInputStream.read(revbuf);
                    if (checkCmdTag(revbuf)) {
                        System.arraycopy(revbuf, 0, mBuffer, mCurrentSize, revbuf.length);
                        mCurrentSize += revbuf.length;
                    }
                } catch (IOException e) {
                }
            }
            int ret = 0;
            for (int i = 0; i < mCurrentSize - 4; ) {
                if (bCancel) {
                    Log.i("xpb", "Cancel=" + String.valueOf(bCancel));
                    return 0;
                }
                if (((byte) mBuffer[i] == (byte) (0xEF)) &&
                        ((byte) mBuffer[i + 1] == (byte) (0x01)) &&
                        ((byte) mBuffer[i + 2] == (byte) (0xFF)) &&
                        ((byte) mBuffer[i + 3] == (byte) (0xFF))
                        ) {
                    int pkgsize = (int) (mBuffer[i + 8]) + ((int) (mBuffer[i + 7] << 8) & 0xFF00) + 9;
                    if (pkgsize == -117)
                        pkgsize = 139;
                    System.arraycopy(mBuffer, i, buffer, ret, pkgsize);
                    ret = ret + pkgsize;
                    i = ret;
                } else {
                    i++;
                }
            }

            return ret;
        }
    }

    protected /*synchronized*/ void write(byte[] data) throws IOException {
        if (mDeviceType != DEV_SPI_3G_7) {
            mCurrentSize = 0;
            mOutputStream.write(data);
        } else {
            if (bCancel) {
                Log.i("xpb", "Cancel=" + String.valueOf(bCancel));
                return;
            }
            byte[] tmp = new byte[150];
            System.arraycopy(data, 0, tmp, 0, data.length);
            mOutputStream.write(tmp);
        }
    }

    private void setUpGpio() throws IOException {
        FPPowerSwitch(true);
    }

    private void setDownGpio() throws IOException {
        FPPowerSwitch(false);
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            while (!isInterrupted()) {
                int length = 0;
                try {
                    byte[] buffer = new byte[100];
                    if (mInputStream == null)
                        return;
                    length = mInputStream.read(buffer);
                    if (length > 0) {
                        System.arraycopy(buffer, 0, mBuffer, mCurrentSize,
                                length);
                        mCurrentSize += length;
                    }
                    Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length="
                            + length);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void Cancel(boolean sw) {
        bCancel = sw;
    }

    public boolean WriteIoFile(String strValue, String Path) {
        File file;
        FileOutputStream outstream;
        try {
            file = new File(Path);
            outstream = new FileOutputStream(file);
            outstream.write(strValue.getBytes());
            outstream.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void IoControl(boolean bOpen) {
        String GPIO_DIR_1 = "/sys/devices/soc.0/scan_se955.69/";
        String GPIO_DIR_2 = "/sys/devices/soc.0/scan_se955.71/";
        String[] GPIO_FILE = {"start_scan",
                "power_status"};
        if (bOpen) {
            WriteIoFile("on", GPIO_DIR_1 + GPIO_FILE[0]);
            WriteIoFile("on", GPIO_DIR_1 + GPIO_FILE[1]);
        } else {
            WriteIoFile("off", GPIO_DIR_1 + GPIO_FILE[0]);
            WriteIoFile("off", GPIO_DIR_1 + GPIO_FILE[1]);
        }
        if (bOpen) {
            WriteIoFile("on", GPIO_DIR_2 + GPIO_FILE[0]);
            WriteIoFile("on", GPIO_DIR_2 + GPIO_FILE[1]);
        } else {
            WriteIoFile("off", GPIO_DIR_2 + GPIO_FILE[0]);
            WriteIoFile("off", GPIO_DIR_2 + GPIO_FILE[1]);
        }
    }

    public int getDeviceType() {
        String devname = android.os.Build.MODEL;
        String devid = android.os.Build.DEVICE;
        String devmodel = android.os.Build.DISPLAY;
        Log.d("SerialPortManager", "xinghao:" + devname);
        if (devname.equals("b82"))
            return DEV_SPI_3G_7;
        if (devname.equals("FP07") || devname.equals("FP-07")) {
            if (devid.equals("b906"))
                return DEV_SPI_3G_7;
            else if (devmodel.indexOf("35SM") >= 0)
                return DEV_USB_4G_7;
            else if (devmodel.indexOf("80M") >= 0)
                return DEV_USB_4G_7;
            return DEV_SPI_3G_7;
        }
        if (devname.equals("FP08") || devname.equals("FP-08T")) {
            Log.d("SerialPortManager", "DEV_USB_4G_8:" + DEV_USB_4G_8);
            return DEV_USB_4G_8;
        }
        if (devname.equals("FT06") || devname.equals("FT-06")) {
            return DEV_UART_3G_ACCESS;
        }
        if (devname.equals("HF-A5")) {
            return DEV_UART_HF_A5;
        }
        if (devname.equals("FP06") || devname.equals("FP-06") || devname.equals("KT7500")) {
            return DEV_UART_4G_6;
        }
        if (devname.equals("M9PLUS")) {
            return DEV_UART_4G_T;
        }
        if (devname.equals("FP-05")) {
            if (devmodel.indexOf("35SM") >= 0) {
                return DEV_UART_4G_5;
            }
            if (devmodel.indexOf("80M") >= 0) {
                return DEV_UART_3G_6;
            }
            if (devmodel.indexOf("37SM") >= 0) {
                return DEV_UART_4G_5;
            }
            return DEV_UART_3G_6;
        }
        if (devname.equals("M9PLUS")) {
            return DEV_UART_3G_5N;

        }
        if (devname.equals("FP--05")) {
            return DEV_UART_4G_7;
        }
        if (devmodel.indexOf("35SM") >= 0) {
            return DEV_UART_4G_5;
        }
        if (devmodel.indexOf("80M") >= 0) {
            return DEV_UART_3G_5N;
        }


        return DEV_UART_3G_5O;
    }

    public String GetUartPath() {
        switch (mDeviceType) {
            case DEV_UART_3G_5O:
                return "/dev/ttyMT3";
            case DEV_UART_3G_5N:
                return "/dev/ttyMT1";
            case DEV_SPI_3G_7:
                return "/dev/spidev0.0";
            case DEV_UART_3G_6:
                return "/dev/ttyMT1";
            case DEV_UART_4G_5:
                return "/dev/ttyMT1";
            case DEV_UART_4G_T:
                return "/dev/ttyHSL1";
            case DEV_UART_4G_6:
                return "/dev/ttyMT1";
            case DEV_USB_4G_7:
                return "";
            case DEV_UART_4G_7:
                return "/dev/ttyMT1";
            case DEV_USB_4G_8:
                return "/dev/ttyMT2";
            case DEV_UART_3G_ACCESS:
                return "/dev/ttyMT1";
            case DEV_UART_HF_A5:
                return "/dev/ttyMT1";
        }
        return "";
    }

    public void FPPowerSwitch(boolean bOn) {
        switch (mDeviceType) {
            case DEV_UART_3G_5O: {
                MtGpio mt = new MtGpio();
                if (bOn) {
                    mt.sGpioMode(119, 0);
                    mt.sGpioDir(119, 1);
                    mt.sGpioOut(119, 1);
                } else {
                    mt.sGpioMode(119, 0);
                    mt.sGpioDir(119, 1);
                    mt.sGpioOut(119, 0);
                }
            }
            break;
            case DEV_UART_3G_5N: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 1);
                } else {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 0);
                }
            }
            break;
            case DEV_SPI_3G_7: {
                if (bOn) {
                    mSerialPort.PowerSwitch(true);
                } else {
                    mSerialPort.PowerSwitch(false);
                }
            }
            break;
            case DEV_UART_3G_6: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(65, 0);
                    ca.setGpioDir(65, 1);
                    ca.setGpioOut(65, 1);
                } else {
                    ca.setGpioMode(65, 0);
                    ca.setGpioDir(65, 1);
                    ca.setGpioOut(65, 0);
                }
            }
            break;
            case DEV_UART_4G_5: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 1);
                } else {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 0);
                }
            }
            break;
            case DEV_UART_4G_T: {
                if (bOn) {
                    IoControl(true);
                } else {
                    IoControl(false);
                }
            }
            break;
            case DEV_UART_4G_6: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 1);
                } else {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 0);
                }
            }
            break;
            case DEV_UART_4G_7: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(59, 0);
                    ca.setGpioDir(59, 1);
                    ca.setGpioOut(59, 1);
                } else {
                    ca.setGpioMode(59, 0);
                    ca.setGpioDir(59, 1);
                    ca.setGpioOut(59, 0);
                }
            }
            break;
            case DEV_USB_4G_7: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(14, 0);
                    ca.setGpioDir(14, 1);
                    ca.setGpioOut(14, 1);
                    //HOST_POWER_EN
                    ca.setGpioMode(84, 0);
                    ca.setGpioDir(84, 1);
                    ca.setGpioOut(84, 1);
                } else {
                    ca.setGpioMode(14, 0);
                    ca.setGpioDir(14, 1);
                    ca.setGpioOut(14, 0);
                    //HOST_POWER_EN
                    ca.setGpioMode(84, 0);
                    ca.setGpioDir(84, 1);
                    ca.setGpioOut(84, 0);
                }
            }
            break;
            case DEV_USB_4G_8: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 1);
                 /*   //HOST_POWER_EN
                    ca.setGpioMode(53, 0);
                    ca.setGpioDir(53, 1);
                    ca.setGpioOut(53, 1);*/
                } else {
                    ca.setGpioMode(54, 0);
                    ca.setGpioDir(54, 1);
                    ca.setGpioOut(54, 0);
                  /*  //HOST_POWER_EN
                    ca.setGpioMode(53, 0);
                    ca.setGpioDir(53, 1);
                    ca.setGpioOut(53, 0);*/
                }
            }
            break;
            case DEV_UART_3G_ACCESS: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(14, 0);
                    ca.setGpioDir(14, 1);
                    ca.setGpioOut(14, 1);
                } else {
                    ca.setGpioMode(14, 0);
                    ca.setGpioDir(14, 1);
                    ca.setGpioOut(14, 0);
                }
            }
            break;
            case DEV_UART_HF_A5: {
                CommonApi ca = new CommonApi();
                if (bOn) {
                    ca.setGpioMode(15, 0);
                    ca.setGpioDir(15, 1);
                    ca.setGpioOut(15, 1);
                } else {
                    ca.setGpioMode(15, 0);
                    ca.setGpioDir(15, 1);
                    ca.setGpioOut(15, 0);
                }
            }
            break;
        }
    }
}
