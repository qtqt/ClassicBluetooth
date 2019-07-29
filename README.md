经典蓝牙的功能的使用，设备的管理，及蓝牙音箱、耳机的连接
具体的功能如下：
```
void init(Context context);

boolean removeBond(BluetoothDevice device);//取消配对

boolean connect(BluetoothDevice device);//连接设备

boolean open();//打开蓝牙

boolean close();//关闭蓝牙

boolean startDiscovery();//搜索蓝牙
boolean stopDiscovery();//停止搜索蓝牙
String getName();//获取本地蓝牙名称
boolean setName(String name);//设置蓝牙的名称
String getAddress();//获取本地蓝牙地址
boolean isEnable();//蓝牙是否可用，即是否打开
boolean isSupport();//是否支持蓝牙
Set<BluetoothDevice> getBondedDevices();//获取以配对设备
boolean createBond(BluetoothDevice device);//配对
boolean disconnect(BluetoothDevice device);//断开设备
void destroy();
void getConnectedDevices();//获取已连接的设备
boolean isConnected(BluetoothDevice device);//是否连接


boolean setDiscoverableTimeout(int timeout);//设备可见时间

void setBTStateListener(IBTStateListener btStateListener);//蓝牙状态监听(开关监听)

void setBTScanListener(IBTScanListener btScanListener);//蓝牙搜索监听

void setBTBoudListener(IBTBoudListener btBoudListener);//蓝牙绑定监听

void setBTConnectListener(IBTConnectListener btConnectListener);//设置连接监听
 ```
    使用时，直接使用BluetoothHelper即可，[详细介绍](https://blog.csdn.net/qtiao/article/details/97654675)
