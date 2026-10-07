package com.kencyue.accsvviewer;

import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.webkit.*;import java.io.*;import java.util.Base64;

public class MainActivity extends Activity {
 WebView web; byte[] pendingCsv=null;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(android.graphics.Color.rgb(23,105,170));getWindow().setNavigationBarColor(android.graphics.Color.rgb(244,246,248));web=new WebView(this);setContentView(web);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);web.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){deliver();}});web.addJavascriptInterface(new Bridge(),"AndroidCSV");readIntent(getIntent());web.loadUrl("file:///android_asset/www/index.html");}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);readIntent(i);deliver();}
 void readIntent(Intent i){if(i!=null&&Intent.ACTION_VIEW.equals(i.getAction())&&i.getData()!=null){try(InputStream in=getContentResolver().openInputStream(i.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[65536];int n;while((n=in.read(buf))>0)out.write(buf,0,n);pendingCsv=out.toByteArray();}catch(Exception e){pendingCsv=null;}}}
 void deliver(){if(pendingCsv==null)return;web.evaluateJavascript("window.__openAndroidCsv&&window.__openAndroidCsv()",null);}
 class Bridge { @JavascriptInterface public void pick(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/*");startActivityForResult(i,7);});} @JavascriptInterface public int size(){return pendingCsv==null?0:pendingCsv.length;} @JavascriptInterface public String chunk(int off,int len){if(pendingCsv==null||off>=pendingCsv.length)return "";int n=Math.min(len,pendingCsv.length-off);return Base64.getEncoder().encodeToString(java.util.Arrays.copyOfRange(pendingCsv,off,off+n));} @JavascriptInterface public void consumed(){pendingCsv=null;} }
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==7&&c==RESULT_OK&&d!=null){Intent i=new Intent(Intent.ACTION_VIEW,d.getData());readIntent(i);deliver();}}
}
