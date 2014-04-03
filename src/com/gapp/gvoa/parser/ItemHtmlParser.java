package com.gapp.gvoa.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

import com.gapp.gvoa.datatype.RssItem;

public class ItemHtmlParser  {

	public static final String tag = "GVOA.ItemHtmlParser";	
	
	
	public static void parseItemDetail(RssItem item)
	{
		/*
		if(null==item.getLink())
		{
			return;
		}*/
		//String testurl ="http://www.51voa.com/VOA_Standard_English/us-weighs-boosting-training-for-syrian-rebels-52551.html";
		
		try {
		    Document doc = Jsoup.connect(item.getLink()).get();
		    Element content = doc.getElementById("content");
		    item.setFullText(content.text());
        	Element mp3link = content.select("a[id=mp3]").first();
        	if(mp3link!=null)
        	{
        	    Log.i(tag,mp3link.attr("href")); 
        	}
        	Element lrclink = content.select("a[id=lrc]").first();
        	if(lrclink != null)
        	{
        	    Log.i(tag,lrclink.attr("href")); 
        	}
		}
	    catch( Exception e ) {
	        Log.e(tag, "parse failed:"+item.getLink(), e) ;
	    }
        return ;
	}
	

	

}