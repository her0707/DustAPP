package com.myapp.dustapp;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity implements OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String from = "WGS84";
    String to = "TM";

    static TextView pm10value, pm10grade, location, pm25value, khaivalue;
    static ImageButton refresh, add, menu;
    static ListView listView;
    static DrawerLayout DrawerLayout;
    static ActionBarDrawerToggle drawerToggle;
    static int stationCnt = 0;
    static Context mContext;
    static Toolbar toolbar;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ChildEventListener mChild;
    Date dt = new Date();

    SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");
    String getTime = full_sdf.format(dt);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }


        init();
        initDatabase();
        final ArrayList<ItemData> oData = new ArrayList<>();
        final ListAdapter oAdapter = new ListAdapter(oData);
        listView.setAdapter(oAdapter);

        drawerToggle = new ActionBarDrawerToggle(this, DrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        DrawerLayout.setDrawerListener(drawerToggle);

        mGoogleApiClient.connect();
        databaseReference.child("data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // 클래스 모델이 필요?
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    ItemData oItem = new ItemData();
                    oItem.strTitle = fileSnapshot.child("station").getValue(String.class);
                    oItem.strPm10 = fileSnapshot.child("pm10").getValue(String.class);
                    oItem.strDate = getTime;
                    oData.add(oItem);

                }
                oAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
            }
        });
    }

    public void init() {
        mContext = getApplicationContext();
        location = (TextView) findViewById(R.id.location);
        pm10value = (TextView) findViewById(R.id.pm10value);
        pm10grade = (TextView) findViewById(R.id.pm10grade);
        pm25value = (TextView) findViewById(R.id.pm25value);
        khaivalue = (TextView) findViewById(R.id.khaivalue);
        refresh = (ImageButton) findViewById(R.id.refresh);
        menu = (ImageButton) findViewById(R.id.menu);
        add = (ImageButton) findViewById(R.id.add);
        listView = (ListView)findViewById(R.id.listView);
        DrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);



        refresh.setOnClickListener(this);
        add.setOnClickListener(this);
        menu.setOnClickListener(this);



        mGoogleApiClient = new GoogleApiClient.Builder(this)    //google service
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                String stationName;
                stationName = location.getText().toString();
                getFindDust(stationName);

                break;
            case R.id.add:
                AddStation Add = new AddStation(location.getText().toString(),pm10value.getText().toString(),pm25value.getText().toString(),khaivalue.getText().toString());
                databaseReference.child(location.getText().toString()).setValue(Add);

                break;
            case R.id.menu:
                DrawerLayout.openDrawer(listView);
                break;

            default:

                break;
        }
    }

    public class ItemData {
                public String strTitle;
                public String strDate;
                public String strPm10;
            }

            public class ListAdapter extends BaseAdapter {
                LayoutInflater inflater = null;
                private ArrayList<ItemData> m_oData = null;
                private int nListCnt = 0;

                public ListAdapter(ArrayList<ItemData> _oData) {
                    m_oData = _oData;
                    nListCnt = m_oData.size();
                }

                @Override
                public int getCount() {
                    Log.i("TAG", "getCount");
                    return nListCnt;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final Context context = parent.getContext();
                if (inflater == null) {
                        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                convertView = inflater.inflate(R.layout.list_view, parent, false);
            }

            TextView oTextTitle = (TextView) convertView.findViewById(R.id.textTitle);
            TextView oTextDate = (TextView) convertView.findViewById(R.id.textDate);
            TextView oTextPm10 = (TextView) convertView.findViewById(R.id.textPm10);

            oTextTitle.setText(m_oData.get(position).strTitle);
            oTextDate.setText(m_oData.get(position).strDate);
            oTextPm10.setText(m_oData.get(position).strPm10);
            return convertView;
        }
    }
    private void initDatabase() {

        mChild = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(mChild);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mChild);
    }
    public static void getFindDust(String name) {    //대기정보를 가져오는 스레드

        GetFindDustThread.active = true;
        GetFindDustThread getweatherthread = new GetFindDustThread(false, name);        //스레드생성(UI 스레드사용시 system 뻗는다)
        getweatherthread.start();    //스레드 시작

    }


    public static void FindDustThreadResponse(String getCnt, String[] sPm10Value, String[] sPm10Grade, String[] sKhaiValue, String[] sPm25Value24) {    //대기정보 가져온 결과값
        stationCnt = 0;    //측정개수정보(여기선 1개만 가져온다
        stationCnt = Integer.parseInt(getCnt);


        if (stationCnt == 0) {             //만약 측정정보가 없다면
            pm10value.setText("");
            pm10grade.setText("");
            pm25value.setText("");
            khaivalue.setText("");
        } else {    //측정정보있으면
            pm10value.setText(sPm10Value[0]);
            pm10grade.setText(transGrade(sPm10Grade[0]));
            pm25value.setText(sPm25Value24[0]);
            khaivalue.setText(sKhaiValue[0]);
        }

        GetFindDustThread.active = false;
        GetFindDustThread.interrupted();
    }


    public static void getNearStation(String yGrid, String xGrid) {    //이건 측정소 정보가져올 스레드

        GetStationListThread.active = true;
        GetStationListThread getstationthread = new GetStationListThread(false, yGrid, xGrid);        //스레드생성(UI 스레드사용시 system 뻗는다)
        getstationthread.start();    //스레드 시작

    }

    public static void NearStationThreadResponse(String[] sStation, String[] sAddr, String[] sTm) {    //측정소 정보를 가져온 결과
        location.setText(sStation[0]);
        GetFindDustThread.active = false;
        GetFindDustThread.interrupted();
    }

    void getStation(String yGrid, String xGrid) {

        if (xGrid != null && yGrid != null) {
            GetTransCoordThread.active = true;
            GetTransCoordThread getCoordthread = new GetTransCoordThread(false, xGrid, yGrid, from, to);        //스레드생성(UI 스레드사용시 system 뻗는다)
            getCoordthread.start();    //스레드 시작
        } else {
            Toast.makeText(getApplication(), "좌표값 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public static void TransCoordThreadResponse(String x, String y) {    //대기정보 가져온 결과값
        if (x.equals("NaN") || y.equals("NaN")) {
            location.setText("오류");
        } else {
            getNearStation(y, x);
        }
        GetTransCoordThread.active = false;
    }

    static public String transGrade(String intGrade) {
        String trans = null;
        switch (intGrade) {
            case "1":
                trans = "좋음";
                break;
            case "2":
                trans = "보통";
                break;
            case "3":
                trans = "나쁨";
                break;
            case "4":
                trans = "매우나쁨";
                break;
            default:
                break;

        }
        return trans;
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("mLastLocation", String.valueOf(mLastLocation.getLatitude()) + "," + mLastLocation.getLongitude());
        if (mLastLocation != null) {
            //totalcnt.setText(String.valueOf(mLastLocation.getLatitude()) + "," + mLastLocation.getLongitude());
            getStation(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        } else {
            location.setText("오류");
        }
        mGoogleApiClient.disconnect();
    }

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}




}



