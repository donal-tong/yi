package com.studio.b56.im.app.ui.group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.annotation.view.ViewInject;
import net.tsz.afinal.core.FileNameGenerator;
import net.tsz.afinal.http.AjaxCallBack;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemLongClickListener;

import com.alibaba.fastjson.JSON;
import com.studio.b56.im.R;
import com.studio.b56.im.app.Constants;
import com.studio.b56.im.app.action.AudioPlayListener;
import com.studio.b56.im.app.action.AudioRecorderAction;
import com.studio.b56.im.app.action.ImageInfoAction;
import com.studio.b56.im.app.action.ImageInfoAction.OnBitmapListener;
import com.studio.b56.im.app.api.AlbumApi;
import com.studio.b56.im.app.api.ErrorCode;
import com.studio.b56.im.app.api.FriendApi;
import com.studio.b56.im.app.api.PanLvApi.ClientAjaxCallback;
import com.studio.b56.im.app.control.BaseDialog;
import com.studio.b56.im.app.control.ReaderImpl;
import com.studio.b56.im.app.dao.SessionManager;
import com.studio.b56.im.app.ui.BaseActivity;
import com.studio.b56.im.app.ui.ChatMainActivity;
import com.studio.b56.im.app.ui.CurrentMapActivity;
import com.studio.b56.im.app.ui.FriendChattingActivity;
import com.studio.b56.im.app.ui.MyChattingActivity;
import com.studio.b56.im.app.ui.ShowChatPhotoActivity;
import com.studio.b56.im.app.ui.StrangerChattingActivity;
import com.studio.b56.im.app.ui.adpater.EmojiAdapter;
import com.studio.b56.im.app.ui.adpater.EmojiUtil;
import com.studio.b56.im.app.ui.common.ChatPrompt;
import com.studio.b56.im.app.ui.common.FinalOnloadBitmap;
import com.studio.b56.im.app.ui.common.ChatPrompt.ChatPromptLisenter;
import com.studio.b56.im.app.ui.group.grouplist.DelRoomCallBack;
import com.studio.b56.im.app.vo.MapInfo;
import com.studio.b56.im.command.FileTool;
import com.studio.b56.im.command.TextdescTool;
import com.studio.b56.im.service.SNSGroupManager;
import com.studio.b56.im.service.SnsService;
import com.studio.b56.im.service.receiver.GroupChatMessageReceiver;
import com.studio.b56.im.service.receiver.NotifyChatMessage;
import com.studio.b56.im.service.type.XmppType;
import com.studio.b56.im.vo.CustomerVo;
import com.studio.b56.im.vo.MessageInfo;
import com.studio.b56.im.vo.MessageType;
import com.studio.b56.im.vo.SessionList;
import com.source.widget.DragImageView;

public class group_main extends BaseActivity implements OnBitmapListener{
	public static final String EMOJIREX = "emoji_[\\d]{0,3}";
	private static final int RESQUEST_CODE = 100;
	private static final String TAG = "group_main";
	
	@ViewInject(id = R.id.chat_main_list_msg)
	private ListView listMsgView;
	@ViewInject(id = R.id.chat_box_btn_send)
	private Button btnSend;
	@ViewInject(id = R.id.chat_box_edit_keyword)
	private EditText editSendText;
	@ViewInject(id = R.id.chat_box_btn_info)
	private ToggleButton toggleButton;
	@ViewInject(id = R.id.chat_box_btn_voice)
	private Button btnVoice;
	
	@ViewInject(id = R.id.chat_box_expra_btn_experssion)
	private Button btnEmoji;
	@ViewInject(id = R.id.chat_box_btn_add)
	private Button btnAdd;
	@ViewInject(id = R.id.chat_box_layout_expra)
	private View chatExpra;
	@ViewInject(id = R.id.chat_box_expra_btn_camera)
	private Button btnCamera;
	@ViewInject(id = R.id.chat_box_expra_btn_picture)
	private Button btnPhoto;
	@ViewInject(id = R.id.chat_box_expra_btn_location)
	private Button btnLocation;
	@ViewInject(id = R.id.emoji_grid)
	private GridView emojiGridView;
	
	private View.OnClickListener onClickListener;
	
	private BaseAdapter adapter;
	//private CustomerVo fCustomerVo;
	private SessionList sessionList;
	private List<MessageInfo> messageInfos;
	
	private AlbumApi albumApi;
	
	private ImageInfoAction imageInfoAction;
	private ReaderImpl readerImpl;
	private AudioRecorderAction audioRecorder;
	
	private AudioPlayListener playListener;
	private SessionManager sessionManager;	 // 消息管理。
	
	private AlertDialog messageDialog;		// 消息功能ui
	private View msgDialogContent;  	  //
	
	private AlertDialog moreDialog;
	
	private Handler handler = new Handler();
	
	private DialogBitmap showBitmap;    // 显示图片使用的
	
	private View.OnClickListener showClick;
	//private View.OnClickListener showInfo;
	
	private boolean opconnectState = false;
	
//	private MyBroadcast broadcast;
	private EmojiAdapter mEmojiAdapter;
	public String RoomName="";
	public String RoomJId="";
	private List<String> downVoiceList = new ArrayList<String>();
	public String UserName="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_main);
		baseInit();
	}
	
	@Override
	protected void baseInit(){
		super.baseInit();
		
		RoomName =getIntent().getStringExtra("RoomName");
		
		if(RoomName == null || RoomName==""){
			finish();
			return;
		}
		
		
		audioRecorder = new AudioRecorderAction(getBaseContext());
		readerImpl = new ReaderImpl(group_main.this, handler, audioRecorder){

			@Override
			public void stop(String path) {
				if (TextUtils.isEmpty(path)) {
					showToast("录制时间太短!");
					return;
				}
				File file = new File(path);
				sendVoice(file);
			}
			
		};
		playListener = new AudioPlayListener(getBaseContext()){

			@Override
			public void down(MessageInfo msg) {
				super.down(msg);
				downVoice(msg);
			}
			
		};
		
		opconnectState = isOpconnect();
		
//		broadcast = new MyBroadcast();
		
		registerReceiver();
		
		sessionManager = new SessionManager(getFinalDb(), getPhotoBitmap(), getBaseContext());
		onClickListener = new ViewOnClik();
		
		listMsgView.setOnItemLongClickListener(new ItemOnLongClick());
		
		btnSend.setOnClickListener(onClickListener);
		toggleButton.setOnClickListener(onClickListener);
		btnAdd.setOnClickListener(onClickListener);
		btnCamera.setOnClickListener(onClickListener);
		btnPhoto.setOnClickListener(onClickListener);
		btnLocation.setOnClickListener(onClickListener);
		btnEmoji.setOnClickListener(onClickListener);
		
		btnVoice.setOnTouchListener(new OnVoice());
		
		imageInfoAction = new ImageInfoAction(group_main.this);
		imageInfoAction.setOnBitmapListener(this);
		
		setTitleContent("群:"+RoomName);
		
		initMessageInfos();
		adapter = new MyAdapter();
		listMsgView.setAdapter(adapter);
		
//		ObserverUtils.addObserver(fCustomerVo.getUid(), this);
		listMsgView.setSelection(messageInfos.size());
		albumApi = new AlbumApi();
		clearNotification();
		showClick = new ShowBitmap();
		//showInfo = new ShowInfoClick();
		showBitmap = new DialogBitmap(group_main.this);
		
		if(!opconnectState){
			showToast("正在连接聊天服务器!");
		}
		
		editSendText.setOnFocusChangeListener(sendTextFocusChangeListener);
		editSendText.setOnClickListener(sendTextClickListener);
		mEmojiAdapter = new EmojiAdapter(getBaseContext(), getEmojiList());
		emojiGridView.setAdapter(mEmojiAdapter);
		emojiGridView.setOnItemClickListener(emojiOncListener);
		getHeadBitmap().flushCache();
		getShangwupanlvApplication().setActivity(this);
		
		LoginGroup();
		
		UserName=getMyCustomerVo().getName()+"@"+getMyCustomerVo().getUid();
	}
	
	private void LoginGroup() {
		Log.d(TAG, "sendBroad()");
		Intent intent = new Intent(SNSGroupManager.ACTION_ADD_GROUPCHAT);
		intent.putExtra("group_name", RoomName);
		sendBroadcast(intent);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESQUEST_CODE){
			if(data != null && RESULT_OK == resultCode){
				Bundle bundle = data.getExtras();
				if(bundle != null){
					String city = bundle.getString("city");
					String addr = bundle.getString("addr");
					String lat = bundle.getString("lat");
					String lng = bundle.getString("lon");
					
					if(TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng)){
						showToast("获取经纬度错误!");
						return;
					}
					
					MapInfo mapInfo = new MapInfo();
					mapInfo.setCtiy(city);
					mapInfo.setAddr(addr);
					mapInfo.setLat(lat);
					mapInfo.setLon(lng);
					
					sendMap(mapInfo);
				}
			}
		}else{
			imageInfoAction.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void initMessageInfos() {
		List<SessionList> sessionLists = getFinalDb().findAllByWhere(SessionList.class, "fuid='" + RoomName+"@conference" + "'");
		if(sessionLists != null && !sessionLists.isEmpty()){
			sessionList = sessionLists.get(0);
		}
		
		if(sessionList != null){
			messageInfos = getFinalDb().findAllByWhere(MessageInfo.class, SessionManager.sessionList2MsgWhere(sessionList.getId()) + "  ORDER BY id ASC ");
		}
		
		//messageInfos = getFinalDb().findAllByWhere(MessageInfo.class, " toId='"+ RoomName+"'  ORDER BY id ASC");
		
		if(messageInfos == null){
			messageInfos = new ArrayList<MessageInfo>();
		}
		
		if(messageInfos == null || messageInfos.isEmpty()){
			ChatPrompt.showPrompt(this, new ChatPromptLisenter(this));
		}
	}

	@Override
	protected void initTitle() {
		setBtnBack();
		setTitleRight(R.string.title_more);
	}
	
	/************  发送消息 ************/
	private void send(){
		startService(new Intent(getBaseContext(), SnsService.class));
	}
	// 发送文本
	private void sendText() {
		send();
		Log.d(TAG, "sendText()");
		String str = editSendText.getText().toString();
		if (str != null
				&& (str.trim().replaceAll("\r", "")
						.replaceAll("\t", "").replaceAll("\n", "")
						.replaceAll("\f", "")) != "") {
			editSendText.setText("");
			
			MessageInfo msg = new MessageInfo();
			msg.setContent(str);
			msg.setFromId(getMyCustomerVo().getName()+"@"+getMyCustomerVo().getUid());
			msg.setToId(RoomName);
			msg.setType(MessageType.TEXT);
			msg.setSendTime(System.currentTimeMillis() + "");
			msg.setPullTime(System.currentTimeMillis() + "");
			sendBroad2Save(msg);
		}
	}
	
	private void sendMap(MapInfo mapInfo){
		send();
		MessageInfo msg = new MessageInfo();
		msg.setContent(MapInfo.getInfo(mapInfo));
		msg.setFromId(getMyCustomerVo().getName()+"@"+getMyCustomerVo().getUid());
		msg.setToId(RoomName);
		msg.setType(MessageType.MAP);
		msg.setSendTime(System.currentTimeMillis() + "");
		msg.setPullTime(System.currentTimeMillis() + "");
		sendBroad2Save(msg);
	}
	
	private void sendPhoto(Bitmap bitmap){
		send();
		Log.d(TAG, "sendPhoto()");
		if(bitmap != null){
			String tempUrl = "http://localhost:8080/" + System.currentTimeMillis();
			getPhotoBitmap().addBitmapToCache(tempUrl, bitmap);
			MessageInfo msg = new MessageInfo();
			msg.setContent(tempUrl);
			msg.setFromId(getMyCustomerVo().getName()+"@"+getMyCustomerVo().getUid());
			msg.setToId(RoomName);
			msg.setType(MessageType.PICTURE);
			msg.setSendTime(System.currentTimeMillis() + "");
			msg.setPullTime(System.currentTimeMillis() + "");
			//sendBroad2Update(msg);
			uploadBitmap(msg, bitmap);
		}else{
			showToast("选择图片错误!");
		}
	}
	// 发送音频文件
	// filePath 为文件绝对路径
	private void sendVoice(File filePath){
		send();
		Log.d(TAG, "sendVoice()");
		if(filePath.exists()){
			MessageInfo msg = new MessageInfo();
			msg.setContent(filePath.getName());
			msg.setFromId(getMyCustomerVo().getName()+"@"+getMyCustomerVo().getUid());
			msg.setToId(RoomName);
			msg.setType(MessageType.VOICE);
			msg.setVoiceTime((int)readerImpl.getReaderTime());
			msg.setSendTime(System.currentTimeMillis() + "");
			msg.setPullTime(System.currentTimeMillis() + "");
			//sendBroad2Update(msg);
			uploadVoice(msg, filePath);
			
		}else{
			showToast("录音失败!");
		}
	}
	
	/** 重发语音 */
	private void resendVoice(MessageInfo messageInfo){
		send();
		String url = messageInfo.getContent();
		try {
			if(url.startsWith("http://")){
				sendBroad2Update(messageInfo);
			}else{
				File file = new File(ReaderImpl.getAudioPath(getBaseContext()), url);
				System.out.println("重新发送....:" + file.getAbsolutePath());
				uploadVoice(messageInfo, file);
			}
		} catch (Exception e) {
			Log.d(TAG, "resendVoice:", e);
			showToast("重发失败!");
		}
		
	}
	
	/** 重发图片 */
	private void resendBitmap(MessageInfo messageInfo){
		send();
		String url = messageInfo.getContent();
		try {
			if(!url.startsWith("http://localhost")){
				sendBroad2Update(messageInfo);
			}else{
				Bitmap bitmap = getHeadBitmap().getBitmapFromDiskCache(url);
				if(bitmap != null){
					uploadBitmap(messageInfo, bitmap);
				}else{
					showToast("重发失败!");
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "resendVoice:", e);
			showToast("重发失败!");
		}
	}
	
	// 下载音频
	private synchronized void downVoice(final MessageInfo msg){
		if(!checkNetWorkOrSdcard()){
			return;
		}
		
		if(downVoiceList.contains(msg.getContent())){
			showToast("下载音频");
			return;
		}
		downVoiceList.add(msg.getContent());
		File voicePath = ReaderImpl.getAudioPath(getBaseContext());
		String tag = FileNameGenerator.generator(msg.getContent());
		String tagName = new File(voicePath, tag).getAbsolutePath();
		getFinalHttp().download(msg.getContent(), tagName, new AjaxCallBack<File>() {

			@Override
			public void onSuccess(File t) {
				super.onSuccess(t);
				downVoiceSuccess(msg);
				downVoiceList.remove(msg.getContent());
			}

			@Override
			public void onFailure(Throwable t, String strMsg) {
				super.onFailure(t, strMsg);
				showToast("下载音频出错：" + strMsg);
				downVoiceList.remove(msg.getContent());
			}
			
		});
	}
	
	private void downVoiceSuccess(final MessageInfo msg){
		msg.setSendState(1);
		getFinalDb().update(msg);
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				addMessageInfo(msg);
			}
		});
	}
	
	// 上传音频文件
	private void uploadVoice(final MessageInfo msg, final File filePath){
		Log.d(TAG, "uploadVoice()");
		// 正在发送
		msg.setSendState(2);
		

		if(createSessionList(msg)){
			msg.setSessionId(sessionList.getId());
			getFinalDb().saveBindId(msg);
		};
		
		addMessageInfo(msg);
		
		try {
			albumApi.upload(getUserInfoVo().getUid(), "3", filePath, new ClientAjaxCallback() {

				@Override
				public void onSuccess(String t) {
					super.onSuccess(t);
					String data = ErrorCode.getData(getBaseContext(), t);
					if(data != null){
						try {
							String photoUrl = JSON.parseObject(data).getString("photoUrl");
							String voice = FileNameGenerator.generator(photoUrl);
							FileTool.reNameFile(filePath, voice);
							msg.setContent(photoUrl);
							sendBroad2Update(msg);
						} catch (Exception e) {
							msg.setSendState(0);
							try {
								getFinalDb().update(msg);
							} catch (Exception e2) {
							}
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									addMessageInfo(msg);
								}
							});
						}
						
					}else{
						msg.setSendState(0);
						try {
							getFinalDb().update(msg);
						} catch (Exception e) {
						}
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								addMessageInfo(msg);
							}
						});
					}
				}

				@Override
				public void onFailure(Throwable t, String strMsg) {
					super.onFailure(t, strMsg);
					msg.setSendState(0);
					try {
						getFinalDb().update(msg);
					} catch (Exception e) {
					}
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							addMessageInfo(msg);
						}
					});
					showToast("服务器响应失败");
					try {
						getFinalDb().update(msg);
					} catch (Exception e) {
					}
				}
				
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			msg.setSendState(0);
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					addMessageInfo(msg);
				}
			});
		}
	}
	
	// 上传图片
	private void uploadBitmap(final MessageInfo msg, final Bitmap bitmap){
		Log.d(TAG, "uploadBitmap()");
		
		// 正在发送
		msg.setSendState(2);
		
	
		if(createSessionList(msg)){
			msg.setSessionId(sessionList.getId());
			getFinalDb().saveBindId(msg);
		};
	
		
		addMessageInfo(msg);
		
		albumApi.upload(getUserInfoVo().getUid(), "2", bitmap, new ClientAjaxCallback() {

			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				String data = ErrorCode.getData(getBaseContext(), t);
				if(data != null){
					try {
						String photoUrl = JSON.parseObject(data).getString("photoUrl");
						getHeadBitmap().clearCache(msg.getContent());
						getHeadBitmap().addBitmapToCache(photoUrl, bitmap);
						msg.setContent(photoUrl);
						sendBroad2Update(msg);
					} catch (Exception e) {
						msg.setSendState(0);
						try {
							getFinalDb().update(msg);
						} catch (Exception e2) {
						}
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								addMessageInfo(msg);
							}
						});
					}
					
				}else{
					msg.setSendState(0);
					try {
						getFinalDb().update(msg);
					} catch (Exception e) {
					}
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							addMessageInfo(msg);
						}
					});
				}
			}

			@Override
			public void onFailure(Throwable t, String strMsg) {
				super.onFailure(t, strMsg);
				msg.setSendState(0);
				try {
					getFinalDb().update(msg);
				} catch (Exception e) {
				}
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						addMessageInfo(msg);
					}
				});
				showToast("服务器响应失败");
				try {
					getFinalDb().update(msg);
				} catch (Exception e) {
				}
			}
			
		});
		
	}
	
	/*********  聊天选择器 ***********/
	
	private void togInfoSelect(){
		hideExpra();
		if (toggleButton.isChecked()) {
			// 语音开启显示
			editSendText.setVisibility(View.GONE);
			btnSend.setVisibility(View.GONE);
			btnVoice.setVisibility(View.VISIBLE);
			hideSoftKeyboard(toggleButton);
		} else {
			editSendText.setVisibility(View.VISIBLE);
			btnSend.setVisibility(View.VISIBLE);
			btnVoice.setVisibility(View.GONE);
			hideSoftKeyboard(toggleButton);
		}
	}
	
	@Override
	protected void titleBtnRight() {
		super.titleBtnRight();
		moreAction();
	}

	private void moreAction(){
		if(moreDialog == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(group_main.this);
			builder.setMessage("操作");
			View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.alert_chat_main_more, null);
			builder.setView(view);
			
			Button btnAddFriend = (Button) view.findViewById(R.id.alert_chat_main_more_addfriend);
			Button btnApplyAction = (Button) view.findViewById(R.id.alert_chat_main_more_apply);
			Button btnClear = (Button) view.findViewById(R.id.alert_chat_main_more_btn_clear);
			btnApplyAction.setText("邀请好友");
			btnAddFriend.setText("成员列表");
			btnClear.setOnClickListener(onClickListener);
			btnApplyAction.setOnClickListener(onClickListener);
			btnApplyAction.setVisibility(View.VISIBLE);
			btnAddFriend.setVisibility(View.VISIBLE);
			btnAddFriend.setOnClickListener(onClickListener);
			moreDialog = builder.create();
		}
		
		moreDialog.show();
	}
	
	/* 申请查看信息 */
	private void btnApplyAction(){
		moreDialog.cancel();
		
	}
	
	/* 清除消息 */
	private void btnClearAction(){
		moreDialog.cancel();
		clearMessageAction();
	}
	
	/* 重发信息 */
	private void btnResendAction(View v){
		messageDialog.cancel();
		MessageInfo messageInfo = (MessageInfo) v.getTag();
		if(messageInfo != null){
			switch (messageInfo.getType()) {
			case MessageType.PICTURE:
				resendBitmap(messageInfo);
				break;
			case MessageType.TEXT:
				sendBroad2Update(messageInfo);
				break;
			case MessageType.VOICE:
				resendVoice(messageInfo);
				break;
			case MessageType.MAP:
				sendBroad2Update(messageInfo);
				break;

			default:
				break;
			}
		}
	};
	
	private void btnMapAction(View v) {
		MessageInfo messageInfo = (MessageInfo) v.getTag();
		if(messageInfo != null){
			String content = messageInfo.getContent();
			MapInfo mapInfo = MapInfo.getInfo(content);
			if(mapInfo != null){
				Intent intent = new Intent(this, CurrentMapActivity.class);
				intent.putExtra("tag", "1");
				intent.putExtra("lat", mapInfo.getLat());
				intent.putExtra("lon", mapInfo.getLon());
				intent.putExtra("address", mapInfo.getAddr());
				intent.putExtra("city", mapInfo.getCtiy());
				
				startActivity(intent);
			}
		}
	}
	
	private void btnEmojiAction(){
		showEmojiGridView();
	}
	
	private void btnCameraAction(){
		imageInfoAction.getCameraPhoto();
		hideExpra();
	}
	
	private void btnPhotoAction(){
		imageInfoAction.getLocolPhoto();
		hideExpra();
	}
	
	private void btnLocationAction(){
		Intent intent = new Intent(this, CurrentMapActivity.class);
		startActivityForResult(intent, RESQUEST_CODE);
	}
	
	private void btnAddAction(){
		if(chatExpra.getVisibility() == View.VISIBLE){
			hideExpra();
		}else{
			showExpra();
		}
	}
	
	private void btnCancelMsgDialog(){
		messageDialog.cancel();
	}
	
	private void btnDelMsgAction(View v){
		MessageInfo messageInfo = (MessageInfo)v.getTag();
		delMessageAction(messageInfo);
		btnCancelMsgDialog();
	}
	
	private void showExpra(){
		hideSoftKeyboard();
		chatExpra.setVisibility(View.VISIBLE);
	}
	
	private void hideExpra(){
		chatExpra.setVisibility(View.GONE);
	}
	
	
	private void sendBroad2Update(MessageInfo msg){
		Log.d(TAG, "sendBroad2Update()");
		msg.setSendTime(System.currentTimeMillis() + "");
		msg.setPullTime(System.currentTimeMillis() + "");
		if(isSend()){
			msg.setSendState(1);
			Intent intent = new Intent(GroupChatMessageReceiver.ACTION_ADD_GROUP_SENDCHAT+"_"+RoomName);
			intent.putExtra("extras_messae", msg);
			sendBroadcast(intent);
		}else{
			msg.setSendState(0);
		}
		addMessageInfo(msg);
		try {
			getFinalDb().update(msg);
		} catch (Exception e) {
		}
		
	}
	
	private void sendBroad2Save(MessageInfo msg){
		Log.d(TAG, "sendBroad2Save()");
		addMessageInfo(msg);
		
	
		if(createSessionList(msg)){
			msg.setSessionId(sessionList.getId());
			getFinalDb().saveBindId(msg);
		};
		
		
		if(isSend()){
			msg.setSendState(1);
			Intent intent = new Intent(GroupChatMessageReceiver.ACTION_ADD_GROUP_SENDCHAT+"_"+RoomName);
			intent.putExtra("extras_messae", msg);
			sendBroadcast(intent);
		}else{
			msg.setSendState(0);
		}
	}
	
	private boolean createSessionList(MessageInfo info) {
		if(sessionList != null){
			updateSessionList(info);
			return true;
		}
		
		sessionList = new SessionList();
		sessionList.setCreateTime(System.currentTimeMillis());
		sessionList.setFuid(RoomName+"@conference");
		boolean flag = getFinalDb().saveBindId(sessionList);
		updateSessionList(info);
		return flag;
	}
	
	private void updateSessionList(MessageInfo info){
		if(sessionList == null){
			return;
		}
		sessionList.setNotReadNum(0);
		sessionList.setFuid(RoomName+"@conference");
		sessionList.setUpdateTime(System.currentTimeMillis());

		switch (info.getType()) {
		case MessageType.TEXT:
			sessionList.setLastContent(info.getContent());
			break;
		case MessageType.PICTURE:
			sessionList.setLastContent("图片");
			break;
		case MessageType.VOICE:
			sessionList.setLastContent("语音");
			break;
		case MessageType.MAP:
			sessionList.setLastContent("位置信息");
			break;

		default:
			break;
		}
		
		notifySessionList();
	}
	
	private void notifySessionList(){
		// 通知
//		SessionObserver.notifyObserver(sessionList);
		
	//	Intent intent = new Intent(NotifyChatMessage.ACTION_NOTIFY_CHAT_MESSAGE);
	//	intent.putExtra(NotifyChatMessage.EXTRAS_NOTIFY_SESSION_MESSAGE, sessionList);
	//	sendBroadcast(intent);
		
	}

	/***
	 * 是否可用发送
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-6-3<br />
	 * 修改时间:<br />
	 */
	private boolean isSend(){
		return true;
	}
	
	/**
	 * op是否连接
	 * @return
	 * 作者:fighter <br />
	 * 创建时间:2013-6-9<br />
	 * 修改时间:<br />
	 */
	private boolean isOpconnect(){
		return true;
	}
	
	private void addMessageInfo(MessageInfo info){
		if(!messageInfos.contains(info)){
			messageInfos.add(info);
		}
		updateSessionList(info);
		clearNotification();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		updateSessionList();
		unregisterReceiver();
		playListener.stop();
		notifySessionList();
//		ObserverUtils.deleteObserver(fCustomerVo.getUid(), this);
	}
	
	private void updateSessionList(){
		if(sessionList != null){
			sessionList.setNotReadNum(0);
			try {
				getFinalDb().update(sessionList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(SnsService.ACTION_CONNECT_CHANGE);
		filter.addAction(GroupChatMessageReceiver.ACTION_GROUP_SEND_STATE+"_"+RoomName);
		filter.addAction(GroupChatMessageReceiver.ACTION_GROUP_NEWS_MESSAGE+"_"+RoomName);
		filter.addAction(GroupChatMessageReceiver.ACTION_ADD_GROUP_LOGIN_STATUS+"_"+RoomName);
		registerReceiver(chatReceiver, filter);
	}
	
	private void unregisterReceiver(){
		unregisterReceiver(chatReceiver);
	}
	
	/**
	 * 获取表情列表
	 * @return
	 * @author fighter <br />
	 * 创建时间:2013-6-21<br />
	 * 修改时间:<br />
	 */
	 private List<String> getEmojiList(){
	    	List<String> emojiList = new ArrayList<String>();
	    	String baseName = "emoji_";
	    	for (int i = 85; i <= 88; i++) {
	    		emojiList.add(baseName + i);
			}
	    	
	    	for (int i = 340; i <= 363; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 94; i <= 101; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 115; i <= 117; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 364; i <= 373; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 12; i <= 17; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 0; i <= 11; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 18; i <= 84; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 89; i <= 93; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 101; i <= 114; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	for (int i = 114; i <= 339; i++) {
	    		emojiList.add(baseName + i);
	    	}
	    	
	    	
	    	return emojiList;
	    }
	
	/* 清空消息内容 */
	void clearMessageAction(){
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				return sessionManager.clearSessionList(sessionList, messageInfos);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				getWaitDialog().setMessage("清空内容.");
				getWaitDialog().show();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if(result){
					getWaitDialog().setMessage("清除成功");
					messageInfos.clear();
					adapter.notifyDataSetChanged();
				}else{
					getWaitDialog().setMessage("清除失败");
				}
				
				getWaitDialog().dismiss();
			}
			
			
			
		}.execute();
		
	}
	/* 删除消息内容 */
	void delMessageAction(final MessageInfo messageInfo){
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				return sessionManager.delMsgDb(messageInfo);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				getWaitDialog().setMessage("删除内容");
				getWaitDialog().show();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if(result){
					getWaitDialog().setMessage("删除成功");
					messageInfos.remove(messageInfo);
					adapter.notifyDataSetInvalidated();
				}else{
					getWaitDialog().setMessage("删除失败");
				}
				
				getWaitDialog().dismiss();
			}
			
			
			
		}.execute();
	}
	
	void clearNotification(){
	//	getNotificationManager().cancel(fCustomerVo.getUid().hashCode());
	}
	
	/**
	 * 
	 * 功能： 长按item <br />
	 * 日期：2013-6-5<br />
	 * 地点：www.uvcims.com<br />
	 * 版本：ver 1.0<br />
	 * 
	 * @author fighter
	 * @since
	 */
	class ItemOnLongClick implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
				MessageInfo messageInfo = messageInfos.get(position);
				getMessageDialog(messageInfo).show();
			return true;
		}
		
		private AlertDialog getMessageDialog(MessageInfo messageInfo){
			if(messageDialog == null){
				AlertDialog.Builder builder = new AlertDialog.Builder(group_main.this);
				builder.setTitle("消息");
				View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.alert_chat_main_action, null);
				msgDialogContent = view;
				builder.setView(msgDialogContent);
				messageDialog = builder.create();
			}
			
			Button btnCancel = (Button) msgDialogContent.findViewById(R.id.alert_chat_main_btn_cancel);
			Button btnDelMsg = (Button) msgDialogContent.findViewById(R.id.alert_chat_main_btn_delmsg);
			Button btnResend = (Button) msgDialogContent.findViewById(R.id.alert_chat_main_btn_resend);
			try {
				if(messageInfo.getSendState() == 0 && messageInfo.getFromId().equals(getMyCustomerVo().getUid())){
					btnResend.setVisibility(View.VISIBLE);
				}else{
					btnResend.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				btnResend.setVisibility(View.GONE);
			}
			
			btnResend.setTag(messageInfo);
			btnResend.setOnClickListener(onClickListener);
			btnCancel.setOnClickListener(onClickListener);
			btnDelMsg.setOnClickListener(onClickListener);
			btnDelMsg.setTag(messageInfo);
			
			return messageDialog;
		}
	}
	
	/**
	 * 语音按钮触发
	 */
	class OnVoice implements OnTouchListener {
		// 说话键按下和弹起处理事件
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(!checkSdcard()){
					break;
				}
				readerImpl.showDg();
				break;
			case MotionEvent.ACTION_UP:
				readerImpl.cancelDg();
				break;
			}
			return true;
		}

	}
	
	class ViewOnClik implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.chat_box_btn_send:
				sendText();
				break;
		
			case R.id.chat_box_btn_info:
				togInfoSelect();
				break;
			case R.id.chat_box_btn_add:
				btnAddAction();
				break;
			case R.id.chat_box_expra_btn_camera:
				btnCameraAction();
				break;
			case R.id.chat_box_expra_btn_picture:
				btnPhotoAction();
				break;
			case R.id.chat_box_expra_btn_location:
				btnLocationAction();
				break;
			case R.id.alert_chat_main_btn_delmsg:
				btnDelMsgAction(v);
				break;
			case R.id.alert_chat_main_btn_cancel:
				btnCancelMsgDialog();
				break;
			case R.id.alert_chat_main_more_addfriend:
				//btnAddFriendAction();
				Intent intent2 = new Intent(group_main.this, group_userlist.class);
				intent2.putExtra("roomName", RoomName);
				startActivity(intent2);
				break;
			case R.id.alert_chat_main_more_apply:
				//btnApplyAction();
				Intent intent = new Intent(group_main.this, group_adduser.class);
				intent.putExtra("roomName", RoomName);
				startActivity(intent);
				break;
			case R.id.alert_chat_main_more_btn_clear:
				btnClearAction();
				break;
			case R.id.alert_chat_main_btn_resend:
				btnResendAction(v);
				break;
			case R.id.chat_talk_msg_map:
				btnMapAction(v);
				break;
			case R.id.chat_box_expra_btn_experssion:
				btnEmojiAction();
				break;
				
			default:
				break;
			}
			
		}

	}
	
	class ShowBitmap implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			String content = (String) v.getTag();
			if(TextUtils.isEmpty(content)){
				showToast("URL 为空");
			}else{
				Intent intent = new Intent(group_main.this, ShowChatPhotoActivity.class);
				intent.putExtra("data", content);
				startActivity(intent);
			}
//			if(v instanceof ImageView){
//				ImageView imageView = (ImageView)v;
//				Drawable drawable = imageView.getDrawable();
////				Bitmap bitmap = null;
//				try {
////					if(drawable instanceof BitmapDrawable){
////						bitmap = ((BitmapDrawable)drawable).getBitmap();
////					}else if(drawable instanceof TransitionDrawable){
////						Drawable drawable2 = ((TransitionDrawable)drawable).getDrawable(1);
////						if(drawable2 != null && drawable2 instanceof BitmapDrawable){
////							bitmap = ((BitmapDrawable)drawable2).getBitmap();
////						}
////					}
//					
//					
////					Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
//					if(drawable != null){
//						showBitmap.show(drawable);
//						showBitmap.show();
////						Intent intent = new Intent(group_main.this, DialogBitmapActivity.class);
////						intent.putExtra("data", bitmap);
////						startActivity(intent);
//					}
//				} catch (Exception e) {
//					Log.e(TAG, "错误:", e);
//				}
//			}
		}
		
	}
	
	class MyAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return messageInfos.size();
		}

		@Override
		public MessageInfo getItem(int arg0) {
			return messageInfos.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = 0;
			MessageInfo messageInfo = getItem(position);
			
			if(messageInfo.getFromId().equals(UserName)){
				type = 0;
			}else{
				type = 1;
			}
			
			ViewHolder viewHolder = null;
			if(convertView == null){
				if(1 == type){
					convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.chat_talk_left, null);
				}else{
					convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.chat_talk_right, null);
				}
				viewHolder = ViewHolder.getInstance(convertView);
				viewHolder.flag = type;
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
				if(type != viewHolder.flag){
					if(1 == type){
						convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.chat_talk_left, null);
					}else{
						convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.chat_talk_right, null);
					}
					viewHolder = ViewHolder.getInstance(convertView);
					viewHolder.flag = type;
				}
			}
			
			convertView.setTag(viewHolder);
			bindView(viewHolder, messageInfo);
			return convertView;
		}
		
		private void bindView(ViewHolder viewHolder, MessageInfo messageInfo){
			CustomerVo customerVo = null;
			if(0 == viewHolder.flag){
				customerVo = getMyCustomerVo();
				if(0 == messageInfo.getSendState()){
					viewHolder.imgSendState.setVisibility(View.VISIBLE);
				}else{
					viewHolder.imgSendState.setVisibility(View.GONE);
				}
				
			}else{
				customerVo = getMyCustomerVo();
			}
			viewHolder.imgHead.setMaxWidth(0);
			viewHolder.imgHead.setMinimumWidth(0);
			viewHolder.txtNickName.setVisibility(View.VISIBLE);
			
			viewHolder.txtNickName.setText(messageInfo.getFromId().split("@")[0]);
			viewHolder.txtNickName.setTag(customerVo);
		
	
			

			
			
			viewHolder.txtTime.setText(TextdescTool.getTime(messageInfo.getSendTime()));
			switch (messageInfo.getType()) {
			case MessageType.TEXT:
				viewHolder.txtMsgMap.setVisibility(View.GONE);
				viewHolder.txtVoiceNum.setVisibility(View.GONE);
				if(viewHolder.wiatProgressBar != null){
					viewHolder.wiatProgressBar.setVisibility(View.GONE);
				}
				viewHolder.imgMsgVoice.setVisibility(View.GONE);
				viewHolder.imgMsgPhoto.setVisibility(View.GONE);
				viewHolder.txtMsg.setVisibility(View.VISIBLE);
				viewHolder.txtMsg.setText(EmojiUtil.getExpressionString(getBaseContext(), messageInfo.getContent(), EMOJIREX));
				break;
			case MessageType.PICTURE:
				viewHolder.txtMsgMap.setVisibility(View.GONE);
				String content = messageInfo.getContent();
				viewHolder.txtVoiceNum.setVisibility(View.GONE);
				if(content.startsWith("http://localhost") && 2 == messageInfo.getSendState()){
					if(viewHolder.wiatProgressBar != null){
						viewHolder.imgMsgVoice.setVisibility(View.GONE);
						viewHolder.imgMsgPhoto.setVisibility(View.GONE);
						viewHolder.txtMsg.setVisibility(View.GONE);
						viewHolder.wiatProgressBar.setVisibility(View.VISIBLE);
					}
				}else{
					if(viewHolder.wiatProgressBar != null){
						viewHolder.wiatProgressBar.setVisibility(View.GONE);
					}
					viewHolder.imgMsgVoice.setVisibility(View.GONE);
					viewHolder.txtMsg.setVisibility(View.GONE);
					getPhotoBitmap().display(viewHolder.imgMsgPhoto, content);
					viewHolder.imgMsgPhoto.setVisibility(View.VISIBLE);
					viewHolder.imgMsgPhoto.setTag(content);
					viewHolder.imgMsgPhoto.setOnClickListener(showClick);
				}
				break;
			case MessageType.VOICE:
				viewHolder.txtMsgMap.setVisibility(View.GONE);
				if(2 == messageInfo.getSendState()){
					if(viewHolder.wiatProgressBar != null){
						viewHolder.imgMsgVoice.setVisibility(View.GONE);
						viewHolder.imgMsgPhoto.setVisibility(View.GONE);
						viewHolder.txtMsg.setVisibility(View.GONE);
						viewHolder.wiatProgressBar.setVisibility(View.VISIBLE);
					}
				}else{
					if(viewHolder.wiatProgressBar != null){
						viewHolder.wiatProgressBar.setVisibility(View.GONE);
					}
					if(4 == messageInfo.getSendState()){
						downVoice(messageInfo);
					}
					viewHolder.imgMsgPhoto.setVisibility(View.GONE);
					viewHolder.imgMsgVoice.setVisibility(View.VISIBLE);
					viewHolder.txtMsg.setVisibility(View.GONE);
					viewHolder.imgMsgVoice.setTag(messageInfo);
					viewHolder.imgMsgVoice.setOnClickListener(playListener);
					viewHolder.txtVoiceNum.setVisibility(View.VISIBLE);
					viewHolder.txtVoiceNum.setText(messageInfo.getVoiceTime() + "''");
					try {
						AnimationDrawable drawable = (AnimationDrawable) viewHolder.imgMsgVoice.getDrawable();
						drawable.stop();
						drawable.selectDrawable(0);
					} catch (Exception e) {
					}
					
				}
				break;
				
			case MessageType.MAP:
				viewHolder.txtVoiceNum.setVisibility(View.GONE);
				if(viewHolder.wiatProgressBar != null){
					viewHolder.wiatProgressBar.setVisibility(View.GONE);
				}
				viewHolder.imgMsgVoice.setVisibility(View.GONE);
				viewHolder.imgMsgPhoto.setVisibility(View.GONE);
				viewHolder.txtMsg.setVisibility(View.GONE);
				viewHolder.txtMsgMap.setVisibility(View.VISIBLE);
				try {
					MapInfo mapInfo = MapInfo.getInfo(messageInfo.getContent());
					if(mapInfo != null){
						viewHolder.txtMsgMap.setText(mapInfo.getAddr());
					}
				} catch (Exception e) {
				}
				
				viewHolder.txtMsgMap.setTag(messageInfo);
				viewHolder.txtMsgMap.setOnClickListener(onClickListener);
				break;
			default:
				break;
			}
		}
		
	}
	
	static class ViewHolder {
		int flag = 0;    // 1 好友 0 自己
		TextView txtTime, txtMsg, txtVoiceNum, txtMsgMap,txtNickName;
		ImageView imgHead, imgMsgPhoto, imgMsgVoice, imgSendState;
		ProgressBar wiatProgressBar;
		LinearLayout layoutinfo1;
		public static ViewHolder getInstance(View view){
			ViewHolder holder = new ViewHolder();
			holder.txtTime = (TextView) view.findViewById(R.id.chat_talk_txt_time);
			holder.txtMsg = (TextView) view.findViewById(R.id.chat_talk_msg_info_text);
			holder.txtMsgMap = (TextView) view.findViewById(R.id.chat_talk_msg_map);
			holder.txtNickName= (TextView) view.findViewById(R.id.chat_talk_usernick);
			holder.imgHead = (ImageView) view.findViewById(R.id.chat_talk_img_head);
			holder.imgMsgPhoto = (ImageView) view.findViewById(R.id.chat_talk_msg_info_msg_photo);
			holder.imgMsgVoice = (ImageView) view.findViewById(R.id.chat_talk_msg_info_msg_voice);
			
			holder.imgSendState = (ImageView) view.findViewById(R.id.chat_talk_msg_sendsate);
			holder.wiatProgressBar = (ProgressBar) view.findViewById(R.id.chat_talk_msg_progressBar);
			holder.txtVoiceNum = (TextView) view.findViewById(R.id.chat_talk_voice_num);
			holder.layoutinfo1 = (LinearLayout)view.findViewById(R.id.chat_talk_msg_info);
			
			return holder;
		}
	}
	
	class DialogBitmap extends Dialog{
		private Bitmap drawable;
		private DragImageView imageView;
		private ViewTreeObserver viewTreeObserver;
		private LinearLayout linearLayout;
		private int state_height;// 状态栏的高度
		
		private int window_width, window_height;// 控件宽度
		
		public DialogBitmap(Context context) {
			super(context, R.style.DialogPrompt);
			this.setCanceledOnTouchOutside(true);
			this.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					if(drawable != null){
//						drawable.recycle();
					}
				}
			});
		}
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.bitmap_show);
			imageView = (DragImageView) this.findViewById(R.id.show_bitmap);
			linearLayout = (LinearLayout) this.findViewById(R.id.layout);
			
			viewTreeObserver = imageView.getViewTreeObserver();
			WindowManager manager = getWindowManager();
			window_width = manager.getDefaultDisplay().getWidth();
			window_height = manager.getDefaultDisplay().getHeight();
			
			imageView.setmActivity(group_main.this);
			
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				
				@Override
				public void onGlobalLayout() {
					if (state_height == 0) {
						// 获取状况栏高度
						Rect frame = new Rect();
						getWindow().getDecorView()
								.getWindowVisibleDisplayFrame(frame);
						state_height = frame.top;
						imageView.setScreen_H(window_height-state_height);
						imageView.setScreen_W(window_width);
					}
					
				}
				
			});
			if(drawable != null){
				imageView.setImageBitmap(drawable);
			}
			
			initParams();
		}
		private void initParams(){
			try {
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels);
				linearLayout.setLayoutParams(params);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		public void show(Drawable drawable){
			try {
				if(drawable instanceof BitmapDrawable){
				
					Bitmap tempBitmap = ((BitmapDrawable)drawable).getBitmap();
					if(tempBitmap != null){
						this.drawable = tempBitmap;
					}
					
				} else if(drawable instanceof TransitionDrawable){
					TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
					Drawable drawable2 = transitionDrawable.getDrawable(1);
					if(drawable2 != null && drawable2 instanceof BitmapDrawable){
						BitmapDrawable bd = (BitmapDrawable)drawable2;
						this.drawable = bd.getBitmap();
					}
				}
				
				if(this.drawable == null){
					cancel();
				}
				
				if(imageView != null){
					imageView.setImageBitmap(this.drawable);
				}
				
			} catch (Exception e) {
				Log.d(TAG, "BitmapShow Error", e);
				cancel();
			}
		}
		
	}
	

//	@Override
//	public void update(Observable observable, Object data) {
//		final MessageInfo msg = (MessageInfo) data;
//		
//		handler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					// 当该信息不来自好友就过滤掉!
//					if(msg.getFromId().equals(fCustomerVo.getUid())){
//						addMessageInfo(msg);
//					}
//				} catch (Exception e) {
//				}
//				
//			}
//		});
//	}

	@Override
	public void getToBitmap(int bimType, Bitmap bm) {
		sendPhoto(bm);
	}
	
	public Bitmap getMapBitmap(String filename){
		FileInputStream fileinputstream;
		try {
			fileinputstream = (new FileInputStream(filename));
			Bitmap bitmap = BitmapFactory.decodeStream(fileinputstream);
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
			
	}
	

	/**************        表情功能       *************/
	
	// 显示表情列表
	private void showEmojiGridView(){
		hideExpra();
		toggleButton.setChecked(false);
		togInfoSelect();
		emojiGridView.setVisibility(View.VISIBLE);
	}
	
	// 隐藏表情列表
	private void hideEmojiGridView(){
		hideExpra();
		
		emojiGridView.setVisibility(View.GONE);
	}
	
	private AdapterView.OnItemClickListener emojiOncListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(view instanceof ImageView){
				Drawable drawable = ((ImageView) view).getDrawable();
				if(drawable instanceof BitmapDrawable){
					Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
					String name = mEmojiAdapter.getItem(position);
					
					Drawable mDrawable = new BitmapDrawable(getResources(), bitmap);
					int width = getResources().getDimensionPixelSize(R.dimen.pl_emoji);
			        int height = width;
			        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0); 
					ImageSpan span = new ImageSpan(mDrawable);
					
					SpannableString spannableString = new SpannableString("[" + name + "]");
					//类似于集合中的(start, end)，不包括起始值也不包括结束值。
					// 同理，Spannable.SPAN_INCLUSIVE_EXCLUSIVE类似于 [start，end)
					spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					Editable dEditable = editSendText.getEditableText();
					int index = editSendText.getSelectionStart();
					dEditable.insert(index, spannableString);
				}
			}
		}
	};
	
	private View.OnFocusChangeListener sendTextFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				// 文本框得到焦点，隐藏附加信息和表情列表
				hideEmojiGridView();
			}
			
		}
	};
	
	private View.OnClickListener sendTextClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// 获取到文本框的点击事件隐藏表情
			hideEmojiGridView();
		}
	};
	
	
	/**  聊天广播 */
     private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(SnsService.ACTION_CONNECT_CHANGE.equals(action)){
				Log.d(TAG, "receiver:" + action);
				String type = intent.getExtras().getString(SnsService.EXTRAS_CHANGE);
				Log.d(TAG, "receiver:Exper" + type);
				if(XmppType.XMPP_STATE_AUTHENTICATION.equals(type)){
					// 认证成功
					opconnectState = true;
				}else if(XmppType.XMPP_STATE_AUTHERR.equals(type)){
					// 认证失败
					opconnectState = false;
					showToast("用户登录信息认证失败!");
				}else if(XmppType.XMPP_STATE_REAUTH.equals(type)){
					// 未认证
					opconnectState = false;
				}else if(XmppType.XMPP_STATE_START.equals(type)){
					// 开始登录
					opconnectState = false;
				}else if(XmppType.XMPP_STATE_STOP.equals(type)){
					// 没开启登录
					opconnectState = false;
				}
			}else if((GroupChatMessageReceiver.ACTION_GROUP_SEND_STATE+"_"+RoomName).equals(action)){
				Log.d(TAG, "receiver:" + (GroupChatMessageReceiver.ACTION_GROUP_SEND_STATE+"_"+RoomName));
				final MessageInfo messageInfo = (MessageInfo) intent.getSerializableExtra("extras_message");
				
				if(messageInfo != null){
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							int index = messageInfos.indexOf(messageInfo);
							if(index != -1){
								MessageInfo tempInfo = messageInfos.get(index);
								tempInfo.setSendState(messageInfo.getSendState());
								adapter.notifyDataSetInvalidated();
								Log.d(TAG, "信息姿状态已更新==="+tempInfo.getContent());
							}
							
						}
					});
				}
			}else if((GroupChatMessageReceiver.ACTION_GROUP_NEWS_MESSAGE+"_"+RoomName).equals(action)){
				final MessageInfo msg = (MessageInfo) intent.getSerializableExtra("extras_message");
				if(msg == null){
					return;
				}
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						try {
								addMessageInfo(msg);
						} catch (Exception e) {
						}
						
					}
				});
				}
			
			    else if((GroupChatMessageReceiver.ACTION_ADD_GROUP_LOGIN_STATUS+"_"+RoomName).equals(action)){
				int code=intent.getIntExtra("GROUP_LOGIN_CODE",0);
				if(code==0){
					
					 new BaseDialog.Builder(group_main.this).setTitle("提示").setMessage("加入群聊失败，可能网络超时，或者群组不存在，请稍后再试!").setYesListener(new BaseDialog.YesListener()
				      {
				        public void doYes()
				        {
                           finish();
				        }
				      }).setCancelListener(new BaseDialog.CancelListener(){
						@Override
						public void doCancel() {
						}}).setNoCancel(true).show();
					 
				}
				else
				{
					Log.v(TAG, "==登入聊天室成功!!");
				}
			}
			
		}
	};
	
}
