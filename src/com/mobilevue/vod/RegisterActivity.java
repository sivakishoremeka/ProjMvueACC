package com.mobilevue.vod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.mobilevue.data.ClientData;
import com.mobilevue.data.ClientResponseData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	public static String TAG = "RegisterActivity";
	private final static String NETWORK_ERROR = "Network error.";
	public final static String PREFS_FILE = "PREFS_FILE";
	private ProgressDialog mProgressDialog;
	Handler handler = null;
	EditText et_MobileNumber;
	EditText et_FirstName;
	EditText et_LastName;
	EditText et_EmailId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		et_MobileNumber = (EditText) findViewById(R.id.a_reg_et_mobile_no);
		et_FirstName = (EditText) findViewById(R.id.a_reg_et_first_name);
		et_LastName = (EditText) findViewById(R.id.a_reg_et_last_name);
		et_EmailId = (EditText) findViewById(R.id.a_reg_et_email_id);

	}

	public void btnSubmit_onClick(View v) {
		String email = et_EmailId.getText().toString().trim();
		String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
		if (email.matches(emailPattern)) {
			ClientData client = new ClientData();
			client.setPhone(et_MobileNumber.getText().toString());
			client.setFirstname(et_FirstName.getText().toString());
			client.setLastname(et_LastName.getText().toString());
			client.setEmail(et_EmailId.getText().toString());
			CreateClient(client);
		} else {
			Toast.makeText(RegisterActivity.this,
					"Please enter valid Email Id", Toast.LENGTH_LONG).show();
		}
	}

	public void btnCancel_onClick(View v) {
		closeApp();
	}

	private void closeApp() {
			AlertDialog dialog = new AlertDialog.Builder(RegisterActivity.this,
					AlertDialog.THEME_HOLO_DARK).create();
			dialog.setIcon(R.drawable.img_acc_confirm_dialog);
			dialog.setTitle("Confirmation");
			dialog.setMessage("Do you want to close the app?");
			dialog.setCancelable(false);

			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int buttonId) {
							RegisterActivity.this.finish();
						}
					});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int buttonId) {

						}
					});
			dialog.show();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			closeApp();
		}
		return super.onKeyDown(keyCode, event);
	}


	private void CreateClient(ClientData client) {
		new CreateClientAsyncTask().execute(client);
	}

	private class CreateClientAsyncTask extends
			AsyncTask<ClientData, Void, ResponseObj> {
		ClientData clientData;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(RegisterActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Registering Details");
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
		protected ResponseObj doInBackground(ClientData... arg0) {
			Log.d(TAG, "doInBackground");
			clientData = (ClientData) arg0[0];
			ResponseObj resObj = new ResponseObj();
			{
				if (Utilities.isNetworkAvailable(getApplicationContext())) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("TagURL", "clients");
					map.put("officeId", "1");
					map.put("dateFormat", "dd MMMM yyyy");
					map.put("lastname", clientData.getLastname());
					map.put("firstname", clientData.getFirstname());
					map.put("middlename", "");
					map.put("locale", "en");
					map.put("fullname", "");
					map.put("externalId", "");
					map.put("clientCategory", "20");
					map.put("active", "false");
					map.put("flag", "false");
					map.put("activationDate", "");
					map.put("addressNo", "ghcv");
					map.put("street", "hyderabad");
					map.put("city", "hyderabad");
					map.put("state", "ANDHRA PRADESH");
					map.put("country", "India");
					map.put("zipCode", "436346");
					map.put("phone", clientData.getPhone());
					map.put("email", clientData.getEmail());
					resObj = Utilities.callExternalApiPostMethod(
							getApplicationContext(), map);
					// Log.d("RegAct-CreateClient", resObj.getsResponse());
				} else {
					resObj.setFailResponse(100, NETWORK_ERROR);
					// return resObj;
				}
				if (resObj.getStatusCode() == 200) {
					ClientResponseData clientResData = readJsonUser(resObj
							.getsResponse());
					int clientId = clientResData.getClientId();
					SharedPreferences mPrefs = getSharedPreferences(
							AuthenticationAcitivity.PREFS_FILE, 0);
					Editor mPrefsEditor = mPrefs.edit();
					mPrefsEditor.putInt("CLIENTID", clientId);
					mPrefsEditor.commit();
					if (Utilities.isNetworkAvailable(getApplicationContext())) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("TagURL", "ownedhardware/" + clientId);
						map.put("itemType", "1");// paymentInfo.getClientId());
						map.put("dateFormat", "dd MMMM yyyy");
						String androidId = Settings.Secure.getString(
								getApplicationContext().getContentResolver(),
								Settings.Secure.ANDROID_ID);
						map.put("serialNumber", androidId);// paymentInfo.getPaymentDate());
						map.put("provisioningSerialNumber", "PROVISIONINGDATA");// paymentInfo.getPaymentCode());
						Date date = new Date();
						SimpleDateFormat df = new SimpleDateFormat(
								"dd MMMM yyyy", new Locale("en"));
						String formattedDate = df.format(date);
						map.put("allocationDate", formattedDate);
						map.put("locale", "en");
						map.put("status", "");
						resObj = Utilities.callExternalApiPostMethod(
								getApplicationContext(), map);
					} else {
						resObj.setFailResponse(100, NETWORK_ERROR);
					}
				}
			}
			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {

			super.onPostExecute(resObj);
			Log.d(TAG, "onPostExecute");
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (resObj.getStatusCode() == 200) {
				Log.d("RegAct-H/w Allocan", resObj.getsResponse());
				Intent intent = new Intent(RegisterActivity.this,
						PlanActivity.class);
				RegisterActivity.this.finish();
				startActivity(intent);
			} else
				Toast.makeText(RegisterActivity.this,
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private ClientResponseData readJsonUser(String jsonText) {
		Log.i("readJsonUser", "result is \r\n" + jsonText);
		ClientResponseData response = new ClientResponseData();
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			response = mapper.readValue(jsonText, ClientResponseData.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
