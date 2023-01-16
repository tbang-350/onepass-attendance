package android.zyapi;


public class CommonApi {
    private static CommonApi mMe = null;

    public CommonApi() {
    }

    // gpio
    public native int setGpioMode(int pin, int mode);  //每个GPIO引脚支持四种模式，0为GPIO模式
    public native int setGpioDir(int pin, int dir);    //设置输入还是输出  0是输入1是输出
    public native int setGpioPullEnable(int pin, int enable); //设置上下拉使能
    public native int setGpioPullSelect(int pin, int select);//上拉或下拉
    public native int setGpioOut(int pin, int out); //在一个GPIO口被配置为输出口之后，输出值可以被配置为高(1)或低（0）
    public native int getGpioIn(int pin);  //在一个GPIO口被配置为输入口之后，输入值可以被配置为高(1)或低（0）
    //serialport
    public native int openCom(String port, int baudrate, int bits, char event, int stop);
    public native int openComEx(String port, int baudrate, int bits, char event, int stop, int flags);
    public native int writeCom(int fd, byte[] buf, int sizes);
    public native int readCom(int fd, byte[] buf, int sizes);
    public native int readComEx(int fd, byte[] buf, int sizes, int sec, int usec);
    public native void closeCom(int fd);

    static {
        System.loadLibrary("zyapi_common");
    }
}