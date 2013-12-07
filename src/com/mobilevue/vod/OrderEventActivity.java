package com.mobilevue.vod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.mobilevue.data.ResponseObj;
import com.mobilevue.data.VideoUriData;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;

public class OrderEventActivity extends Activity {

	public static String TAG = "RegisterActivity";
	private final static String NETWORK_ERROR = "Network error.";
	private ProgressDialog mProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_order_event);
		orderEvent();
	}

	private void orderEvent() {
		// TODO Auto-generated method stub
		new orderEventAsyncTask().execute();
	}

	private class orderEventAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {

		protected void onPreExecute() {
			super.onPreExecute();
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(OrderEventActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retriving Detials");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
					cancel(true);
				}
			});
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(Void... arg0) {
			Log.d(TAG, "doInBackground");
			ResponseObj resObj = new ResponseObj();
			Intent i = getIntent();
			Bundle extras = i.getExtras();
			String eventId = extras.getString("eventid");
			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
					new Locale("en"));
			String formattedDate = df.format(date);
			String androidId = Settings.Secure.getString(
					getApplicationContext().getContentResolver(),
					Settings.Secure.ANDROID_ID);

			if (Utilities.isNetworkAvailable(getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TagURL", "eventorder");
				map.put("formatType", "HD");// paymentInfo.getClientId());
				map.put("dateFormat", "yyyy-mm-dd");
				map.put("optType", "RENT");// paymentInfo.getPaymentDate());
				map.put("deviceId", androidId);// paymentInfo.getPaymentCode());
				map.put("eventBookedDate", formattedDate);
				map.put("eventId", eventId);// event.getEventId();125;eventId
				map.put("locale", "en");
				resObj = Utilities.callExternalApiPostMethod(
						getApplicationContext(), map);
				// Log.d("RegAct-CreateClient", resObj.getsResponse());
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
				// return resObj;
			}
			return resObj;
		}

		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			// Log.d(TAG, "onPostExecute");
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				VideoUriData data = readJsonUserVideoUri(resObj.getsResponse());

				Intent intent = new Intent(OrderEventActivity.this,
						VideoPlayerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("URL", data.getResourceIdentifier());
				intent.putExtras(bundle);
				OrderEventActivity.this.finish();
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(),
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
		}

		private VideoUriData readJsonUserVideoUri(String jsonText) {
			Log.i("readJsonUser", "result is \r\n" + jsonText);
			VideoUriData data = new VideoUriData();
			try {
				ObjectMapper mapper = new ObjectMapper().setVisibility(
						JsonMethod.FIELD, Visibility.ANY);
				mapper.configure(
						DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
				data = mapper.readValue(jsonText, VideoUriData.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;

		}

	}
}