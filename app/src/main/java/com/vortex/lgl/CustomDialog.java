package com.vortex.lgl;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CustomDialog extends Dialog {

    private String title;
    private String message;
    private View customView;
    
    private String positiveButtonText;
    private String negativeButtonText;
    private String neutralButtonText;
    
    private DialogInterface.OnClickListener positiveListener;
    private DialogInterface.OnClickListener negativeListener;
    private DialogInterface.OnClickListener neutralListener;
    
    private boolean cancelable = true;
    private boolean centerLayout = false;

    private CustomDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Main container
        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);
        mainLayout.setBackgroundColor(Color.WHITE);
        
        // Apply rounded corners (bottom left and top right only for center layout)
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        if (centerLayout) {
            float[] radii = new float[]{
                50f, 50f,    // top left
                0f, 0f,  // top right
                50f, 50f,  // bottom right
                0f, 0f     // bottom left
            };
            background.setCornerRadii(radii);
        } else {
            background.setCornerRadius(32f);
        }
        mainLayout.setBackground(background);
        
        // Title
        if (title != null && !title.isEmpty()) {
            TextView titleView = new TextView(getContext());
            titleView.setText(title);
            titleView.setTextColor(Color.BLACK);
            titleView.setTextSize(20);
            titleView.setPadding(10, 10, 10, 20);
            titleView.setGravity(Gravity.CENTER);
            mainLayout.addView(titleView);
        }
        
        // Message or Custom View
        if (customView != null) {
            if (centerLayout) {
                // Center the custom view
                LinearLayout.LayoutParams customParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                customParams.setMargins(0, 10, 0, 20);
                customParams.gravity = Gravity.CENTER;
                customView.setLayoutParams(customParams);
                mainLayout.addView(customView);
            } else {
                ScrollView scrollView = new ScrollView(getContext());
                scrollView.addView(customView);
                LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                scrollParams.setMargins(0, 0, 0, 20);
                scrollView.setLayoutParams(scrollParams);
                mainLayout.addView(scrollView);
            }
        } else if (message != null && !message.isEmpty()) {
            TextView messageView = new TextView(getContext());
            messageView.setText(message);
            messageView.setTextColor(Color.BLACK);
            messageView.setTextSize(16);
            messageView.setPadding(10, 10, 10, 20);
            if (centerLayout) {
                messageView.setGravity(Gravity.CENTER);
            }
            mainLayout.addView(messageView);
        }
        
        // Buttons layout
        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(centerLayout ? Gravity.CENTER : Gravity.END);
        buttonLayout.setPadding(0, 10, 0, 0);
        
        // Neutral button
        if (neutralButtonText != null) {
            Button neutralButton = createButton(neutralButtonText, true);
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (neutralListener != null) {
                        neutralListener.onClick(CustomDialog.this, DialogInterface.BUTTON_NEUTRAL);
                    }
                    dismiss();
                }
            });
            
            LinearLayout.LayoutParams neutralParams = new LinearLayout.LayoutParams(
                    centerLayout ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (!centerLayout) {
                neutralParams.weight = 1;
                neutralParams.setMargins(0, 0, 10, 0);
            }
            neutralButton.setLayoutParams(neutralParams);
            buttonLayout.addView(neutralButton);
        }
        
        // Negative button (Cancel)
        if (negativeButtonText != null) {
            Button negativeButton = createButton(negativeButtonText, true);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeListener != null) {
                        negativeListener.onClick(CustomDialog.this, DialogInterface.BUTTON_NEGATIVE);
                    }
                    dismiss();
                }
            });
            
            LinearLayout.LayoutParams negativeParams = new LinearLayout.LayoutParams(
                    centerLayout ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (!centerLayout) {
                negativeParams.weight = 1;
                negativeParams.setMargins(0, 0, 10, 0);
            }
            negativeButton.setLayoutParams(negativeParams);
            buttonLayout.addView(negativeButton);
        }
        
        // Positive button (OK)
        if (positiveButtonText != null) {
            Button positiveButton = createButton(positiveButtonText, false);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (positiveListener != null) {
                        positiveListener.onClick(CustomDialog.this, DialogInterface.BUTTON_POSITIVE);
                    }
                    dismiss();
                }
            });
            
            LinearLayout.LayoutParams positiveParams = new LinearLayout.LayoutParams(
                    centerLayout ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (!centerLayout) {
                positiveParams.weight = 1;
            }
            positiveButton.setLayoutParams(positiveParams);
            buttonLayout.addView(positiveButton);
        }
        
        mainLayout.addView(buttonLayout);
        setContentView(mainLayout);
        setCancelable(cancelable);
        
        // Make dialog background transparent to show rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        if (getWindow() != null) {
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.80);
            getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }
    
    private Button createButton(String text, boolean isOutline) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setAllCaps(false);
        button.setPadding(20, 15, 20, 15);
        
        GradientDrawable buttonBG = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{ Color.parseColor("#4A148C"), Color.parseColor("#000000") });
        buttonBG.setCornerRadius(20f);
        
        float[] radii2 = new float[]{
            50f, 50f,    // top left
            0f, 0f,  // top right
            50f, 50f,  // bottom right
            0f, 0f     // bottom left
        };
        buttonBG.setCornerRadii(radii2);
        if (isOutline) {
            // Outline button (white background, black border)
            //buttonBG.setColor(Color.WHITE);
            buttonBG.setStroke(3, Color.BLACK);
            button.setTextColor(Color.BLACK);
        } else {
            // Filled button (black background, white text)
            //buttonBG.setColor(Color.BLACK);
            button.setTextColor(Color.WHITE);
        }
        
        button.setBackground(buttonBG);
        return button;
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private View customView;
        
        private String positiveButtonText;
        private String negativeButtonText;
        private String neutralButtonText;
        
        private DialogInterface.OnClickListener positiveListener;
        private DialogInterface.OnClickListener negativeListener;
        private DialogInterface.OnClickListener neutralListener;
        
        private boolean cancelable = true;
        private boolean centerLayout = false;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setView(View view) {
            this.customView = view;
            return this;
        }

        public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = text;
            this.positiveListener = listener;
            return this;
        }

        public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = text;
            this.negativeListener = listener;
            return this;
        }

        public Builder setNeutralButton(String text, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = text;
            this.neutralListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setCenterLayout(boolean centerLayout) {
            this.centerLayout = centerLayout;
            return this;
        }

        public CustomDialog create() {
            CustomDialog dialog = new CustomDialog(context);
            dialog.title = this.title;
            dialog.message = this.message;
            dialog.customView = this.customView;
            dialog.positiveButtonText = this.positiveButtonText;
            dialog.negativeButtonText = this.negativeButtonText;
            dialog.neutralButtonText = this.neutralButtonText;
            dialog.positiveListener = this.positiveListener;
            dialog.negativeListener = this.negativeListener;
            dialog.neutralListener = this.neutralListener;
            dialog.cancelable = this.cancelable;
            dialog.centerLayout = this.centerLayout;
            return dialog;
        }

        public CustomDialog show() {
            CustomDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}