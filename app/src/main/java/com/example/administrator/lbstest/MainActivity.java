package com.example.administrator.lbstest;

//com.example.administrator.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    public TextView positionView;
    private MapView bdmapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());  //一定要在setContentView之前调用，初始化操作
        setContentView(R.layout.activity_main);
        positionView= (TextView) findViewById(R.id.position_text_view);
        bdmapView=(MapView) findViewById(R.id.bdmapview);
        baiduMap=bdmapView.getMap();                 //地图总控器，可对地图进行各种操作如缩放及移到某一经纬度上
        baiduMap.setMyLocationEnabled(true);         //设置可以在地图上显示当前设备位置

        /*
        运行时权限处理，判断是否授权
        @param permissionList用于存储还未授权需要进行请求的权限
         */
        List<String>permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }if(!permissionList.isEmpty()){
            String []permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);


        }else{
            requestLocation();
        }


    }
        /*
        请求地址信息，调用start方法即可，请求到的位置信息会回调
         */
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    /*
    创建LocationClientOption对象，调用setScanSpan方法设置更新时间，每5秒更新一下，因为start（）方法只会定位一次
    option.setIsNeedAddress(true);获取当前位置的详细看得懂的位置信息，getCountry()等方法，二不只是经纬度信息
     */
    public void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }


 /*
  *请求到的位置信息会回调到这个方法里进行处理，如果有用户不同意的权限存在，即刻finish掉活动
 */

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                          //  return;
                        }

                    }
                  requestLocation();


                }else{
                    Toast.makeText(MainActivity.this,"未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:


        }

    }
    /*
    MyLocationData.Builder 用来封装设备当前位置信息
    build()方法，把要封装的信息设置完后，调用就会生成一个MyLocationData对象。再传入到
    baiduMap.setMyLocationData中即可。

     */
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        MyLocationData.Builder locationBulder =new MyLocationData.Builder();
        locationBulder.longitude(location.getLongitude());
        locationBulder.latitude(location.getLatitude());
        MyLocationData myLocationData=locationBulder.build();
        baiduMap.setMyLocationData(myLocationData);

    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation||bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                navigateTo(bdLocation);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition=new StringBuilder();
                    currentPosition.append("纬度：").append(bdLocation.getLatitude()).
                            append("\n");
                    currentPosition.append("经度：").append(bdLocation.getLongitude()).
                            append("\n");
                    currentPosition.append("国家：").append(bdLocation.getCountry()).
                            append("\n");
                    currentPosition.append("省份：").append(bdLocation.getProvince()).
                            append("\n");
                    currentPosition.append("市：").append(bdLocation.getCity()).
                            append("\n");
                    currentPosition.append("区：").append(bdLocation.getDistrict()).
                            append("\n");
                    currentPosition.append("街道：").append(bdLocation.getStreet()).
                            append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");

                    }
                    positionView.setText(currentPosition);

                }
            });

        }
    }
    /*
    活动销毁时一定要 mLocationClient.stop()来停止定位，不然程序会在后天不停的进行定位，消耗手机电量
     */
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        bdmapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);

    }
    @Override
    protected void onPause(){
        super.onPause();
        bdmapView.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        bdmapView.onResume();
    }

}
