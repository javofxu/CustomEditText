package com.example.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by 许格.
 * Date on 2019/5/14.
 * dec:自定义输入框（可删除，隐藏显示）
 */
public class CustomEditText extends RelativeLayout implements View.OnFocusChangeListener, View.OnClickListener, TextWatcher, CompoundButton.OnCheckedChangeListener
        , PopupWindow.OnDismissListener, AccountControlListener, ViewTreeObserver.OnGlobalLayoutListener {

    private boolean isError;

    private boolean isEyeShow;

    private boolean isSupportChinese;

    public static final String TAG = "LoginEditText";
    private View mView;
    private ImageView mIcon;
    private EditText mContent;
    private RelativeLayout mRootView;
    private int iconNormalId;
    private int iconSelectedId;
    private boolean isPassword;
    private boolean canDrop;
    private boolean isEditEnable;
    private boolean isDelShow;

    private ImageButton mDeleteBtn;

    //输入表情前EditText中的文本
    private String inputAfterText;
    //是否重置了EditText的内容
    private boolean resetText;
    private CheckBox mDropArrow;

    private List<String> mDropList;
    private boolean isPopupShow;
    private String hintText;
    private String text;
    private String content;

    private String errorInfo;

    private boolean isIconShow;
    private boolean isDismiss;
    private ImageButton mErrorBtn;
    private CheckBox mEyeBtn;


    private Context context;

    private boolean hasFocus;


    public CustomEditText(Context context) {
        this(context, null, -1);

    }

    public CustomEditText(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText);
        iconNormalId = typedArray.getResourceId(R.styleable.CustomEditText_icon_normal, R.mipmap.delete_icon);
        iconSelectedId = typedArray.getResourceId(R.styleable.CustomEditText_icon_selected, R.mipmap.delete_icon);
        hintText = typedArray.getString(R.styleable.CustomEditText_hintText);
        content = typedArray.getString(R.styleable.CustomEditText_contents);
        isPassword = typedArray.getBoolean(R.styleable.CustomEditText_isPassword, false);
        canDrop = typedArray.getBoolean(R.styleable.CustomEditText_canDrop, false);
        isEyeShow = typedArray.getBoolean(R.styleable.CustomEditText_isEyeShow, false);
        isSupportChinese = typedArray.getBoolean(R.styleable.CustomEditText_isSupportChinese, false);
        isEditEnable = typedArray.getBoolean(R.styleable.CustomEditText_isEditEnable, true);
        isDelShow = typedArray.getBoolean(R.styleable.CustomEditText_isDelShow, true);
        isIconShow = typedArray.getBoolean(R.styleable.CustomEditText_is_icon_show, true);
        typedArray.recycle();
    }

    private void init(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.custom_eidt_text, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRootView = (RelativeLayout) findViewById(R.id.custom_root_view);
        mIcon = (ImageView) findViewById(R.id.custom_icon);
        mContent = (EditText) findViewById(R.id.custom_edit);
        mDeleteBtn = (ImageButton) findViewById(R.id.custom_delete_btn);
        mDropArrow = (CheckBox) findViewById(R.id.custom_drop_arrow);
        mErrorBtn = (ImageButton) findViewById(R.id.custom_error_btn);
        mEyeBtn = (CheckBox) findViewById(R.id.custom_eye);
        mContent.setOnFocusChangeListener(this);
        mContent.addTextChangedListener(this);
        mContent.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mRootView.setOnClickListener(this);
        mDropArrow.setOnClickListener(this);
        mErrorBtn.setOnClickListener(this);
        mDropArrow.setOnCheckedChangeListener(this);
        mEyeBtn.setOnCheckedChangeListener(this);
        setFocusVisible(mContent.hasFocus());
        if (isPassword) {
            mContent.setInputType(129);
            mContent.setTypeface(Typeface.DEFAULT);
        }
        mContent.setHint(hintText);
        mContent.setText(content);

        if (isIconShow) {
            mIcon.setVisibility(View.VISIBLE);
        } else {
            mIcon.setVisibility(View.GONE);
        }

        if (canDrop) {
            mDropArrow.setVisibility(View.VISIBLE);
        } else {
            mDropArrow.setVisibility(View.GONE);
        }

        if (isEyeShow) {
            mEyeBtn.setVisibility(View.VISIBLE);
        } else {
            mEyeBtn.setVisibility(View.GONE);
        }

        mContent.setFocusable(isEditEnable);
    }

    /**
     * 设置EditText的内容
     */
    public void setText(String content) {
        mContent.setText(content);
    }

    /**
     * 获取EditText的内容
     *
     * @return EditText中的内容
     */
    public String getText() {
        return mContent.getText().toString();
    }


    /**
     * 设置下拉参加的数据
     *
     * @param dropList 下拉数据
     */
    public void setDropList(List<String> dropList) {
        this.mDropList = dropList;
    }

    /**
     * 内容是否为空
     *
     * @return 内容是否为空
     */
    public boolean isEmpty() {
        return mContent.getText().length() == 0;
    }


    /**
     * 设置错误内容
     *
     * @param errorInfo 错误信息的内容
     */
    public void setErrorInfo(String errorInfo) {
        if (!isError) {
            isError = true;
        }

        this.errorInfo = errorInfo;
        //mRootView.setBackgroundResource(R.drawable.login_view_bg_error);
        mErrorBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.setVisibility(View.GONE);

//        mContent.clearFocus();
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int i = v.getId();
        if (i == R.id.custom_edit) {
            this.hasFocus = hasFocus;
            Log.d(TAG, "has focus:" + hasFocus);
            setFocusVisible(hasFocus || !isEmpty());
//                clearIconVisible(hasFocus && !isEmpty());

        }
    }

    /**
     * 清除错误状态
     */
    private void clearErrorState() {
        if (isError && hasFocus) {
            isError = false;
            mErrorBtn.setVisibility(View.GONE);
            mContent.setText("");
//            setFocusVisible(hasFocus);
            errorInfo = null;
//            clearIconVisible(false);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.custom_root_view) {
            if (!mContent.hasFocus()) {
                //请求焦点
                mContent.requestFocus();
                //弹出键盘
                InputMethodManager inputManager =
                        (InputMethodManager) mContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mContent, mContent.getText().length());
            }

//                clearErrorState();

        } else if (i == R.id.custom_delete_btn) {
            mContent.setText("");

        } else if (i == R.id.custom_error_btn) {
            showErrorDialog();

        } else if (i == R.id.custom_drop_arrow) {
            isPopupShow = !isPopupShow;

        } else if (i == R.id.custom_edit) {//                clearErrorState();

        } else {
        }


    }

    /**
     * 展示错误提示框
     */
    private void showErrorDialog() {
        if (TextUtils.isEmpty(errorInfo)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(errorInfo);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!resetText) {
            // 那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
            // inputAfterText也就改变了，那么表情过滤就失败了
            inputAfterText = s.toString();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (onHasTextListener != null) {
            onHasTextListener.hasText(this, s.length() > 0);
        }

        if (mContent.hasFocus()) {
            clearIconVisible(s.length() > 0);
        }

        if (!isError) {
            setFocusVisible(mContent.hasFocus() || s.length() > 0);
        }

        if (!resetText) {
            if (count >= 1) {//字符长度最小为1 A编码
                CharSequence input = s.subSequence(start, start + count);
                if (isSupportChinese) {
                    return;
                }
            }
        } else {
            resetText = false;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i = buttonView.getId();
        if (i == R.id.custom_drop_arrow) {
            Log.d(TAG, "isChecked：" + isChecked);
            if (isChecked) {
                if (isDismiss) {
                    mDropArrow.setChecked(false);
                    isDismiss = false;
                    return;
                }
            }

        } else if (i == R.id.custom_eye) {
            if (isChecked) {
                mContent.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                mContent.setInputType(129);
                mContent.setTypeface(Typeface.DEFAULT);
            }

        }

    }

    @Override
    public void onDismiss() {
        isDismiss = true;
        if (mDropArrow.isChecked()) {
            mDropArrow.setChecked(false);
        }

    }


    /**
     * 设置焦点变化时的UI变化
     *
     * @param hasFocus 是否有焦点
     */
    private void setFocusVisible(boolean hasFocus) {
//        if (isError) {
//            return;
//        }

        if (hasFocus) {
            clearErrorState();
            if (!isError) {
                if (mIcon != null && mIcon.getVisibility() == View.VISIBLE) {
                    mIcon.setImageResource(iconSelectedId);
                }
                mRootView.setBackgroundResource(R.drawable.login_view_bg_focus);
                clearIconVisible(!isEmpty());
            }
        } else {
            if (mIcon != null && mIcon.getVisibility() == View.VISIBLE) {
                mIcon.setImageResource(iconNormalId);
            }
            //mRootView.setBackgroundResource(R.drawable.login_view_bg_normal);
            clearIconVisible(false);
        }
    }

    /**
     * 清除密码图标是否显示
     *
     * @param isVisible 是否显示
     */
    private void clearIconVisible(boolean isVisible) {
        if (isVisible) {
            if (isDelShow)
                mDeleteBtn.setVisibility(View.VISIBLE);
        } else {
            mDeleteBtn.setVisibility(View.GONE);
        }

    }

    @Override
    public void clickItem(String account) {
        mContent.setText(account);
        onLoginAccountControlListener.clickItem(account);

    }

    @Override
    public void deleteItem(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle(R.string.delete_account);
        builder.setMessage(getResources().getString(R.string.app_name));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onLoginAccountControlListener != null) {
                    //界面删除数据源，云端or本地数据库
                    onLoginAccountControlListener.deleteItem(position);
                }
                mDropList.remove(position);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);


    }


    private AccountControlListener onLoginAccountControlListener;

    /**
     * 账户删除的监听
     */
    public void setOnLoginAccountControlListener(AccountControlListener onLoginAccountControlListener) {
        this.onLoginAccountControlListener = onLoginAccountControlListener;
    }


    private OnHasTextListener onHasTextListener;

    public void setOnHasTextListener(OnHasTextListener onHasTextListener) {
        this.onHasTextListener = onHasTextListener;
    }


    @Override
    public void onGlobalLayout() {
        final Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        final int screenHeight = getRootView().getHeight();
        Log.e("TAG", rect.bottom + "#" + screenHeight);
        final int heightDifference = screenHeight - rect.bottom;
        boolean visible = heightDifference > screenHeight / 3;
        if (visible) {
            //TODO 键盘弹出事件的监听
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }




}

