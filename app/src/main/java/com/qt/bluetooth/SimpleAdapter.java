package com.qt.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2019/7/29.
 */

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.MyViewHolder>{
    private List<BtItemBean> datas;
    private ItemClickListener mItemClickListener;
    public SimpleAdapter(){
        datas=new ArrayList<>();
    }

    public void addData(BtItemBean btItemBean){
        datas.add(btItemBean);
        notifyDataSetChanged();
    }

    public void add(int index,BtItemBean btItemBean){
        datas.add(index,btItemBean);
        notifyDataSetChanged();
    }

    public void addDataALL(List<BtItemBean> btItemBeans){
        datas.addAll(btItemBeans);
        notifyDataSetChanged();
    }

    public void clear(){
        datas.clear();
    }

    public List<BtItemBean> getData(){
        return datas;
    }

    public void remove(Object o){
        datas.remove(o);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_bluetooth,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final BtItemBean btItemBean=datas.get(position);
        BluetoothDevice bluetoothDevice=btItemBean.getBluetoothDevice();
        holder.txt_wifi_name.setText(TextUtils.isEmpty(bluetoothDevice.getName())?bluetoothDevice.getAddress():bluetoothDevice.getName());
        //连接状态
        switch (btItemBean.getState()){
            case BtItemBean.STATE_UNCONNECT://未连接
            case BtItemBean.STATE_BOND_NONE://未配对
                holder.txt_link_tips.setText("");
                break;
            case BtItemBean.STATE_BONDING://配对中
                holder.txt_link_tips.setText("配对中");
                break;
            case BtItemBean.STATE_BONDED://已配对
                holder.txt_link_tips.setText("已配对");
                break;
            case BtItemBean.STATE_CONNECTING://连接中
                holder.txt_link_tips.setText("连接中");
                break;
            case BtItemBean.STATE_CONNECTED://已连接
                holder.txt_link_tips.setText("已连接");
                break;
            case BtItemBean.STATE_DISCONNECTING://断开中
                holder.txt_link_tips.setText("断开中");
                break;
            case BtItemBean.STATE_DISCONNECTED://已断开
                holder.txt_link_tips.setText("已保存");
                break;
        }

        int styleMajor = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();//获取蓝牙主要分类
        switch (styleMajor) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
                holder.img_signal.setImageResource(R.mipmap.icon_headset);
                break;
            case BluetoothClass.Device.Major.COMPUTER://电脑
                holder.img_signal.setImageResource(R.mipmap.icon_computer);
                break;
            case BluetoothClass.Device.Major.HEALTH://健康状况
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.IMAGING://镜像，映像
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.MISC://麦克风
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.NETWORKING://网络
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.PERIPHERAL://外部设备
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.PHONE://电话
                holder.img_signal.setImageResource(R.mipmap.icon_phone);
                break;
            case BluetoothClass.Device.Major.TOY://玩具
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.UNCATEGORIZED://未知的
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
            case BluetoothClass.Device.Major.WEARABLE://穿戴设备
                holder.img_signal.setImageResource(R.mipmap.icon_bluetooth);
                break;
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItemClickListener!=null){
                    mItemClickListener.onItemClickListener(btItemBean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas==null?0:datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView img_signal;
        private TextView txt_wifi_name;
        private TextView txt_link_tips;
        private View layout;

        public MyViewHolder(View itemView) {
            super(itemView);
            img_signal = (ImageView) itemView.findViewById(R.id.img_signal);
            txt_wifi_name = (TextView) itemView.findViewById(R.id.txt_wifi_name);
            txt_link_tips = (TextView) itemView.findViewById(R.id.txt_link_tips);
            layout = itemView.findViewById(R.id.layout);
        }
    }

    public void setItemClickListener(ItemClickListener listener){
        mItemClickListener=listener;
    }

    public interface ItemClickListener{
        void onItemClickListener(BtItemBean btItemBean);
    }
}
