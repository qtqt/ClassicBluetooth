package com.qt.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qt.bluetooth.bluetooth.BluetoothHelper;
import com.qt.bluetooth.bluetooth.interfaces.IBTBoudListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTConnectListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTScanListener;
import com.qt.bluetooth.bluetooth.interfaces.IBTStateListener;
import com.qt.bluetooth.bluetooth.interfaces.IBluetoothHelper;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SimpleAdapter.ItemClickListener {
    private TextView mTvName,mTvNameTip,mTvPairedDeviceTip,mTvUseDeviceTip;
    private RecyclerView mRecyclerPaired,mRecyclerUse;
    private SimpleAdapter mPairedAdapter,mUseAdapter;
    private IBluetoothHelper mBluetoothHelper;
    private AlertDialog simpleDialog;
    private Switch mSwBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothHelper=new BluetoothHelper();
        mBluetoothHelper.setBTStateListener(mBTStateListener);//设置打开关闭状态监听
        mBluetoothHelper.setBTScanListener(mBTScanListener);//设置扫描监听
        mBluetoothHelper.setBTBoudListener(mBTBoudListener);//设置配对监听
        mBluetoothHelper.setBTConnectListener(mBTConnectListener);//设置连接监听
        mBluetoothHelper.init(this);
        mRecyclerPaired = (RecyclerView) findViewById(R.id.recycler_view_paired);
        mRecyclerUse = (RecyclerView) findViewById(R.id.recycler_view_use);
        mSwBluetooth = (Switch) findViewById(R.id.sw_bluetooth);

        mSwBluetooth = (Switch) findViewById(R.id.sw_bluetooth);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvNameTip = (TextView) findViewById(R.id.tv_name_tip);
        mTvPairedDeviceTip = (TextView) findViewById(R.id.tv_paired_device_tip);
        mTvUseDeviceTip = (TextView) findViewById(R.id.tv_use_device_tip);


        mRecyclerPaired.setNestedScrollingEnabled(false);
        mPairedAdapter = new SimpleAdapter();
        mRecyclerPaired.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerPaired.addItemDecoration(new SpacesItemDecoration(10));
        mRecyclerPaired.setAdapter(mPairedAdapter);
        mPairedAdapter.setItemClickListener(this);

        mRecyclerUse.setNestedScrollingEnabled(false);
        mUseAdapter = new SimpleAdapter();
        mRecyclerUse.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerUse.setAdapter(mUseAdapter);
        mUseAdapter.setItemClickListener(this);
        mSwBluetooth.setChecked(mBluetoothHelper.isEnable());

        mSwBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isPressed())
                    return;
                if(isChecked){
                    mBluetoothHelper.open();
                }else{
                    mBluetoothHelper.close();
                }
            }
        });

        mTvName.setText(mBluetoothHelper.getName());
        getBondedDevices();
        mBluetoothHelper.getConnectedDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothHelper.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothHelper.stopDiscovery();
    }

    /**
     * 获取已配对设备
     */
    private void getBondedDevices(){//以配对设备
        Set<BluetoothDevice> bluetoothDeviceSet=mBluetoothHelper.getBondedDevices();
        if(bluetoothDeviceSet!=null&&bluetoothDeviceSet.size()>0){
            for(BluetoothDevice device:bluetoothDeviceSet){
                addDevPaire(BtItemBean.STATE_BONDED,device);
            }
        }
    }

    /**
     * 向已配对列表中添加设备
     * @param state
     * @param dev
     */
    private void addDevPaire(int state,BluetoothDevice dev){
        BtItemBean btUseItem=findItemByList(mPairedAdapter.getData(),dev);
        if(btUseItem!=null){
            btUseItem.setBluetoothDevice(dev);
        }else{
            BtItemBean bluetoothItem=createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mPairedAdapter.add(0,bluetoothItem);
        }
        mPairedAdapter.notifyDataSetChanged();
    }

    /**
     * 从集合 datas 中找 dev 对应的项
     * @param datas
     * @param dev
     */
    private BtItemBean findItemByList(List<BtItemBean> datas,BluetoothDevice dev){
        if(datas==null||datas.size()<1){
            return null;
        }
        for(BtItemBean btItemBean:datas){
                if(!TextUtils.isEmpty(dev.getAddress())&&dev.getAddress().equals(btItemBean.getBluetoothDevice().getAddress())){
                    return btItemBean;
                }
        }
        return null;
    }

    private BtItemBean createBluetoothItem(BluetoothDevice device){
        BtItemBean btItemBean=new BtItemBean();
        btItemBean.setBluetoothDevice(device);
        return btItemBean;
    }

    /**
     * 向可用列表中添加设备
     * @param dev
     */
    private void addDevUse(BluetoothDevice dev){
        BtItemBean btUseItem=findItemByList(mUseAdapter.getData(),dev);
        if(btUseItem!=null){
            btUseItem.setBluetoothDevice(dev);
        }else{
            BtItemBean bluetoothItem=createBluetoothItem(dev);
            if(dev.getBondState()==BluetoothDevice.BOND_BONDED){
                bluetoothItem.setState(BtItemBean.STATE_BONDED);
            }else if(dev.getBondState()==BluetoothDevice.BOND_BONDING){
                bluetoothItem.setState(BtItemBean.STATE_BONDING);
            }
            mUseAdapter.add(0,bluetoothItem);
        }
        mUseAdapter.notifyDataSetChanged();
    }

    /**
     * 可用设备列表发生改变
     * @param state
     * @param dev
     */
    private void paireDevStateChange(int state,BluetoothDevice dev){
        BtItemBean btUseItem=findItemByList(mUseAdapter.getData(),dev);
        BtItemBean btPaireItem=findItemByList(mPairedAdapter.getData(),dev);
        if(btUseItem!=null){
            btUseItem.setState(state);
            btUseItem.setBluetoothDevice(dev);
            mUseAdapter.remove(btUseItem);
            mUseAdapter.notifyDataSetChanged();
            if(btPaireItem!=null){
                mPairedAdapter.remove(btPaireItem);
            }
            mPairedAdapter.add(0,btUseItem);
        }else if(btPaireItem!=null){
            btPaireItem.setState(state);
            btPaireItem.setBluetoothDevice(dev);
        }else{
            BtItemBean bluetoothItem=createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mPairedAdapter.add(0,bluetoothItem);
        }
        mPairedAdapter.notifyDataSetChanged();
    }

    /**
     * 可用设备列表发生改变
     * @param state
     * @param dev
     */
    private void useDevStateChange(int state,BluetoothDevice dev){
        BtItemBean btUseItem=findItemByList(mUseAdapter.getData(),dev);
        BtItemBean btPaireItem=findItemByList(mPairedAdapter.getData(),dev);
        if(btPaireItem!=null){
            btPaireItem.setState(state);
            btPaireItem.setBluetoothDevice(dev);
            mPairedAdapter.remove(btPaireItem);
            mPairedAdapter.notifyDataSetChanged();
            if(btUseItem!=null){
                mUseAdapter.remove(btUseItem);
            }
            mUseAdapter.add(0,btPaireItem);
        }else if(btUseItem!=null){
            btUseItem.setState(state);
            btUseItem.setBluetoothDevice(dev);
        }else{
            BtItemBean bluetoothItem=createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mUseAdapter.add(0,bluetoothItem);
        }
        mUseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothHelper.setBTStateListener(null);//设置打开关闭状态监听
        mBluetoothHelper.setBTScanListener(null);//设置扫描监听
        mBluetoothHelper.setBTBoudListener(null);//设置配对监听
        mBluetoothHelper.setBTConnectListener(null);//设置连接监听
        mBluetoothHelper.destroy();
    }

    //蓝牙状态监听
    private IBTStateListener mBTStateListener=new IBTStateListener() {

        /**
         * 蓝牙开关状态
         * int STATE_OFF = 10; //蓝牙关闭
         * int STATE_ON = 12; //蓝牙打开
         * int STATE_TURNING_OFF = 13; //蓝牙正在关闭
         * int STATE_TURNING_ON = 11; //蓝牙正在打开
         */
        @Override
        public void onStateChange(int state) {
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(MainActivity.this,"蓝牙已关闭",Toast.LENGTH_SHORT).show();
                    mSwBluetooth.setChecked(mBluetoothHelper.isEnable());
                    mTvNameTip.setVisibility(View.GONE);
                    mTvName.setVisibility(View.GONE);
                    mTvPairedDeviceTip.setVisibility(View.GONE);
                    mTvUseDeviceTip.setVisibility(View.GONE);
                    mRecyclerPaired.setVisibility(View.GONE);
                    mRecyclerUse.setVisibility(View.GONE);
                    mPairedAdapter.clear();
                    mPairedAdapter.notifyDataSetChanged();
                    mUseAdapter.clear();
                    mUseAdapter.notifyDataSetChanged();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(MainActivity.this,"蓝牙已打开",Toast.LENGTH_SHORT).show();
                    mSwBluetooth.setChecked(mBluetoothHelper.isEnable());
                    mTvNameTip.setVisibility(View.VISIBLE);
                    mTvName.setVisibility(View.VISIBLE);
                    mTvPairedDeviceTip.setVisibility(View.VISIBLE);
                    mTvUseDeviceTip.setVisibility(View.VISIBLE);
                    mRecyclerPaired.setVisibility(View.VISIBLE);
                    mRecyclerUse.setVisibility(View.VISIBLE);
                    getBondedDevices();
                    mBluetoothHelper.setDiscoverableTimeout(300);//设置可见时间
//                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                //讲一个键值对对方到Intent对象当中，用于指定可见状态的持续时间
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
                    mBluetoothHelper.startDiscovery();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
            }
        }
    };

    //蓝牙搜索监听
    private IBTScanListener mBTScanListener=new IBTScanListener() {
        @Override
        public void onScanStart() {//搜索开始
            Toast.makeText(MainActivity.this,"搜索开始",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanStop(List<BluetoothDevice> deviceList) {//搜索结束

        }

        /**
         *
         * @param device
         */
        @Override
        public void onFindDevice(BluetoothDevice device) {//发现新设备
            if(device.getBondState()==BluetoothDevice.BOND_BONDED) {//已配对
                addDevPaire(BtItemBean.STATE_BONDED,device);
            }else{
                addDevUse(device);
            }
        }
    };

    //蓝牙配对监听
    private IBTBoudListener mBTBoudListener=new IBTBoudListener() {

        /**
         * 设备配对状态改变
         * int BOND_NONE = 10; //配对没有成功
         * int BOND_BONDING = 11; //配对中
         * int BOND_BONDED = 12; //配对成功
         */
        @Override
        public void onBondStateChange(BluetoothDevice dev) {
            if(dev.getBondState()==BluetoothDevice.BOND_BONDED) {//已配对
                paireDevStateChange(BtItemBean.STATE_BONDED,dev);
                mBluetoothHelper.connect(dev);
            }else if(dev.getBondState()==BluetoothDevice.BOND_BONDING){//配对中
                useDevStateChange(BtItemBean.STATE_BONDING,dev);
            }else{//未配对
                BtItemBean btUseItem=findItemByList(mUseAdapter.getData(),dev);
                if(btUseItem!=null&&btUseItem.getState()==BtItemBean.STATE_BONDING){
                    Toast.makeText(MainActivity.this,"请确认配对设备已打开且在通信范围内",Toast.LENGTH_SHORT).show();
                }
                useDevStateChange(BtItemBean.STATE_BOND_NONE,dev);
            }
        }
    };

    //蓝牙配对监听
    private IBTConnectListener mBTConnectListener=new IBTConnectListener() {
        @Override
        public void onConnecting(BluetoothDevice bluetoothDevice) {//连接中
            paireDevStateChange(BtItemBean.STATE_CONNECTING,bluetoothDevice);
        }

        @Override
        public void onConnected(BluetoothDevice bluetoothDevice) {//连接成功
            paireDevStateChange(BtItemBean.STATE_CONNECTED,bluetoothDevice);
        }

        @Override
        public void onDisConnecting(BluetoothDevice bluetoothDevice) {//断开中
            paireDevStateChange(BtItemBean.STATE_DISCONNECTING,bluetoothDevice);
        }

        @Override
        public void onDisConnect(BluetoothDevice bluetoothDevice) {//断开
            paireDevStateChange(BtItemBean.STATE_DISCONNECTED,bluetoothDevice);
        }

        @Override
        public void onConnectedDevice(List<BluetoothDevice> devices) {//已连接设备
            if(devices==null||devices.size()<1){
                return;
            }
            for(BluetoothDevice dev:devices){
                BtItemBean btUseItem=findItemByList(mPairedAdapter.getData(),dev);
                if(btUseItem!=null){
                    btUseItem.setBluetoothDevice(dev);
                    if(mBluetoothHelper.isConnected(dev)){
                        btUseItem.setState(BtItemBean.STATE_CONNECTED);
                    }else if( btUseItem.getState()!=BtItemBean.STATE_CONNECTED){
                        btUseItem.setState(BtItemBean.STATE_DISCONNECTED);
                    }
                }else{
                    BtItemBean bluetoothItem=createBluetoothItem(dev);
                    if(mBluetoothHelper.isConnected(dev)){
                        bluetoothItem.setState(BtItemBean.STATE_CONNECTED);
                    }else{
                        btUseItem.setState(BtItemBean.STATE_DISCONNECTED);
                    }
                    mPairedAdapter.add(0,bluetoothItem);
                }
            }
            mPairedAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClickListener(BtItemBean btItemBean) {
        final BluetoothDevice bluetoothDevice=btItemBean.getBluetoothDevice();
        switch (btItemBean.getState()){
            case BtItemBean.STATE_UNCONNECT://未连接
            case BtItemBean.STATE_BOND_NONE://未配对
                mBluetoothHelper.createBond(bluetoothDevice);
                break;
            case BtItemBean.STATE_BONDING://配对中
                break;
            case BtItemBean.STATE_BONDED://已配对
                simpleDialog=new AlertDialog.Builder(this)
                        .setTitle("已配对")
                        .setMessage(TextUtils.isEmpty(bluetoothDevice.getName())?bluetoothDevice.getAddress():bluetoothDevice.getName())
                        .setNegativeButton("取消",null)
                        .setNeutralButton("配对", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                simpleDialog = null;
                                mBluetoothHelper.removeBond(bluetoothDevice);
                            }
                        })
                        .show();
                break;
            case BtItemBean.STATE_CONNECTING://连接中
                break;
            case BtItemBean.STATE_CONNECTED://已连接
                simpleDialog = new AlertDialog.Builder(this)
                        .setTitle("已连接")
                        .setMessage(TextUtils.isEmpty(bluetoothDevice.getName())?bluetoothDevice.getAddress():bluetoothDevice.getName())
                        .setNegativeButton("取消",null)
                        .setNeutralButton("配对", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                simpleDialog = null;
                                mBluetoothHelper.disconnect(bluetoothDevice);
                            }
                        })
                        .show();
                break;
            case BtItemBean.STATE_DISCONNECTING://断开中
                break;
            case BtItemBean.STATE_DISCONNECTED://已断开(但还保存)
                simpleDialog = new AlertDialog.Builder(this)
                        .setTitle("已保存")
                        .setMessage(TextUtils.isEmpty(bluetoothDevice.getName())?bluetoothDevice.getAddress():bluetoothDevice.getName())
                        .setNegativeButton("取消",null)
                        .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                simpleDialog = null;
                                mBluetoothHelper.removeBond(bluetoothDevice);
                            }
                        })
                        .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                simpleDialog = null;
                                mBluetoothHelper.connect(bluetoothDevice);
                            }
                        })
                        .show();
                break;
        }
    }
}
