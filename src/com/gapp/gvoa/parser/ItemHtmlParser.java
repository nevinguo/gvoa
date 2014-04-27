package com.gapp.gvoa.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

import com.gapp.gvoa.datatype.RssItem;

public class ItemHtmlParser  {

	public static final String tag = "ItemHtmlParser";	
	
	
	public static void parseItemDetail(RssItem item) throws  Exception
	{
		/*
		if(null==item.getLink())
		{
			return;
		}*/
		//String testurl ="http://www.51voa.com/VOA_Standard_English/us-weighs-boosting-training-for-syrian-rebels-52551.html";
		

	    Document doc = Jsoup.connect(item.getLink()).get();

    	Element mp3link = doc.select("a[id=mp3]").first();
    	if(mp3link!=null)
    	{
    	    Log.i(tag,mp3link.attr("href")); 
    	    item.setMp3url(mp3link.attr("href"));
    	}
    	else
    	{
    		Log.i(tag,"can't get mp3");
    	}
	    
    	Element content = doc.getElementById("content");	    
	    item.setFullText(content.text());

    	Element lrclink = content.select("a[id=lrc]").first();
    	if(lrclink != null)
    	{
    	    Log.i(tag,lrclink.attr("href")); 
    	}
    	item.setStatus(RssItem.E_PARSE_TXT_OK);

        return ;
	}
	

	

}
