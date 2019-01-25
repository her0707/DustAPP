package com.myapp.dustapp;

import android.os.Handler;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

class GetFindDustThread extends Thread {
	static public boolean active=false;
	int data=0;
	public boolean isreceiver;
	String sTotalCount;
	String[] sDate,sPm10Value,sKhaiValue,sKhaiGrade,sPm25Value24,sPm10Grade;
	boolean bTotalCount,bDate,bPm10Value,bKhaiValue,bKhaiGrade,bPm10Grade,bPm25Value24;
	boolean tResponse;
	String dongName;
	Handler handler;
	String Servicekey="ServiceKey=81Q6FvMj9YUHARkU2nXGFgqQenmi7740o7WesHvF3s22YEU2pd%2FWGlf9ACaaNO%2BNVIjjiZZmiFoIwhaMJMInXQ%3D%3D";
	String getInfo="http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/";
	String getStationFindDust="getMsrstnAcctoRltmMesureDnsty?";
	String searchDate="dataTerm=daily";
	String station="stationName=";
	String infoCnt="numOfRows=50";
	String Version="ver=1.3";


	public GetFindDustThread(boolean receiver, String dong){

		handler=new Handler();
		isreceiver=receiver;
		//dongName=dong;
		try{
			dongName = URLEncoder.encode(dong, "utf-8");
		}catch(Exception e){

		}


		bTotalCount=bDate=bPm10Value=bKhaiValue=bKhaiGrade=bPm10Grade=bPm25Value24=false;
	}
	public void run(){

		if(active){
			try{
                sDate=new String[100];	//측정일

                sKhaiGrade=new String[100];	//통합 대기환경 지수

                sPm10Grade=new String[100];	//미세먼지 지수
				sPm10Value=new String[100];
				sPm25Value24 = new String[100];
				sKhaiValue=new String[100];
                sPm10Grade=new String[100];
				data=0;
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp=factory.newPullParser();
				String dustUrl=getInfo+getStationFindDust+station+dongName+"&"+infoCnt+"&"+searchDate+"&"+Servicekey+"&"+Version;
				Log.w("스레드가 받은 ", dustUrl);
				URL url=new URL(dustUrl);
				InputStream is=url.openStream();
				xpp.setInput(is,"UTF-8");

				int eventType=xpp.getEventType();

				while(eventType!= XmlPullParser.END_DOCUMENT){

					switch(eventType){
					case XmlPullParser.START_TAG:

					    if(xpp.getName().equals("dataTime")){	//측정일
                            bDate=true;


                    } if(xpp.getName().equals("pm10Value")){	//미세먼지 농도
                        bPm10Value=true;

                    }if(xpp.getName().equals("pm25Value24")){	//미세먼지 지수
                        bPm25Value24=true;

                    } if(xpp.getName().equals("khaiValue")){		//통합대기환경수치
                        bKhaiValue=true;

                    } if(xpp.getName().equals("khaiGrade")){	//통합대기환경지수
                        bKhaiGrade=true;



                    }if(xpp.getName().equals("pm10Grade")){	//미세먼지 지수
                        bPm10Grade=true;

                    }if(xpp.getName().equals("totalCount")){	//결과 수
                        bTotalCount=true;

                    }
						break;

					case XmlPullParser.TEXT:

                        if(bDate){				//동네이름
                            sDate[data]=xpp.getText();
                            bDate=false;

                    }  if(bPm10Value){				//풍향
                        sPm10Value[data]=xpp.getText();
                        bPm10Value=false;
                    } if(bPm25Value24){
                        sPm25Value24[data]=xpp.getText();
                        bPm25Value24=false;
                    } if(bKhaiValue){				//습도
                        sKhaiValue[data]=xpp.getText();
                        bKhaiValue=false;
                    } if(bKhaiGrade){				//날씨
                        sKhaiGrade[data]=xpp.getText();
                        bKhaiGrade=false;
                    }if(bPm10Grade){				//날씨
                        sPm10Grade[data]=xpp.getText();
                        bPm10Grade=false;
                    }
                    if(bTotalCount){
                        sTotalCount=xpp.getText();
                        bTotalCount=false;
                    }
                        break;

					case XmlPullParser.END_TAG:

						if(xpp.getName().equals("response")){
							tResponse=true;
							view_text();
						}if(xpp.getName().equals("item")){
							data++;
						}
						break;
					}
					eventType=xpp.next();
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}



	}


	private void view_text(){

		handler.post(new Runnable() {	//기본 핸들러니깐 handler.post하면됨

			@Override
			public void run() {

				active=false;
				if(tResponse){
					tResponse=false;
					data=0;		//
					MainActivity.FindDustThreadResponse(sTotalCount,sPm10Value,sPm10Grade,sKhaiValue,sPm25Value24);

				}


			}
		});
	}
}
