package com.myapp.dustapp;

import android.os.Handler;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;


class GetTransCoordThread extends Thread {	// 스레드
	static public boolean active=false;
	//파서용 변수
	int data=0;			//이건 파싱해서 array로 넣을때 번지
	public boolean isreceiver;
	String getX,getY;	//결과값
	String gridx,gridy,coordfrom,coordto,outputformat;
	Handler handler;	//값 핸들러
	String Servicekey="apikey=2fe260dce50e3dd04c307b2d1b2bd7bf611edb18";
	String getInfo="https://apis.daum.net/local/geo/transcoord?";
	String fromCoord="fromCoord=";
	String toCoord="toCoord=";
	String x="x=";
	String y="y=";
	String format="output=xml";
	boolean parserEnd=false;
	public GetTransCoordThread(boolean receiver, String x, String y, String from, String to){

		handler=new Handler();
		isreceiver=receiver;
		gridx=x;
		gridy=y;
		coordfrom=from;
		coordto=to;
		outputformat=format;
		getX=getY=null;


	}
	public void run(){
		
		if(active){
			try{
				parserEnd=false;
				data=0;
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();	//이곳이 풀파서를 사용하게 하는곳
				factory.setNamespaceAware(true);									//이름에 공백도 인식
				XmlPullParser xpp=factory.newPullParser();							//풀파서 xpp라는 객체 생성
				String CoordUrl=getInfo+Servicekey+"&"+fromCoord+coordfrom+"&"+toCoord+coordto+"&"+x+gridx+"&"+y+gridy+"&"+format;
				URL url=new URL(CoordUrl);		//URL객체생성
				InputStream is=url.openStream();	//연결할 url을 inputstream에 넣어 연결을 하게된다.
				xpp.setInput(is,"UTF-8");			//이렇게 하면 연결이 된다. 포맷형식은 utf-8로
				
				int eventType=xpp.getEventType();	//풀파서에서 태그정보를 가져온다.
					
				while(eventType!= XmlPullParser.END_DOCUMENT){	//문서의 끝이 아닐때
					
					switch(eventType){
					case XmlPullParser.START_TAG:	//'<'시작태그를 만났을때
						String tag = xpp.getName();
						if (tag.compareTo("result") == 0) { //파서가 result  태그를 만나면 x의 y의 속성 값을 각각 getX,getY에 넣음.
							getX=xpp.getAttributeValue(null, "x");
							getY=xpp.getAttributeValue(null, "y");

							parserEnd=true;
						}
						break;
					}
					eventType=xpp.next();
					}
			}catch(Exception e){
				e.printStackTrace();
			}
			if(parserEnd){

				showtext();
			}
		}

		
		
		
	}

	private void showtext(){
		
		handler.post(new Runnable() {	//기본 핸들러니깐 handler.post하면됨
			
			@Override
			public void run() {
				
				active=false;

					MainActivity.TransCoordThreadResponse(getX, getY);

			}
		});
	}
}
