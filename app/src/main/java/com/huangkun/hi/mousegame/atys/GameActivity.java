package com.huangkun.hi.mousegame.atys;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.huangkun.hi.mousegame.R;
import com.huangkun.hi.mousegame.service.MyMusicService;

import java.util.Random;

/**
 * Created by hi on 2016/7/18.
 */
public class GameActivity extends Activity {

    private Button playGame, overGame;
    private TextView times;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private LinearLayout ll_bg_show;
    private CheckBox cb_sound;

    private int bgAtrr[] = new int[9];  //保存九张带有老鼠的背景图
    private int btnAtrr[] = new int[9]; //保存九个洞对应的按钮
    private MyHandler myHandler = new MyHandler();
    private SumTime sumTime;
    private GameTime gameTime;
    private GoTime goTime;
    private ClickTime clickTime;

    private int t = 59;
    private int sumMouse = 0; //弹出老鼠总个数
    private int clickMouse = 0; //用户点中的老鼠个数
    private int mouseCheckedId = 0; //当前弹出图片在数组中的序号

    private boolean flag = true;  //用于限制开始按钮的有效点击次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        initView(); //初始化控件
        initAtrr(); //添加图片id到数组bgAtrr中
        initListener(); //初始化监听器，将监听器与开始和结束按钮进行绑定
        judgeMusic(); //播放背景音乐
    }

    private void judgeMusic() {
        Intent intentService = new Intent(GameActivity.this, MyMusicService.class);

        if (cb_sound.isChecked()) {
            stopService(intentService);
        } else {
            startService(intentService);
        }
    }

    /**
     * 计时器，游戏开始，该线程sleep一分钟，时间到后，终止弹老鼠进程和计时进程并弹出提示框，传入MyHander的参数为0
     */
    class SumTime extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameTime.isStop(); //调用gameTime的isStop方法，使其停止弹出老鼠
            goTime.isStop(); //调用goTime的isStop方法，结束该线程

            Message message = Message.obtain();
            message.what = 0;
            myHandler.sendMessage(message);
        }
    }

    /**
     * 随机弹出老鼠图片，传入到MyHander的参数为1
     */
    class GameTime extends Thread {
        private boolean isStoped = false;

        private void isStop() {
            isStoped = true;
            gameTime.interrupt();
        }

        @Override
        public void run() {
            super.run();
            while (!isStoped) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Message message = Message.obtain(); //获取Message对象
                message.what = 1;
                myHandler.sendMessage(message);
            }
        }
    }

    /**
     * 时间计数,传入到MyHander的参数为2
     */
    class GoTime extends Thread {
        private boolean isStoped = false;

        private void isStop() {
            isStoped = true;
            goTime.interrupt();
        }

        @Override
        public void run() {
            super.run();
            while (!isStoped) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message message = Message.obtain();
                message.what = 2;
                myHandler.sendMessage(message);
            }
        }
    }

    class ClickTime implements Runnable {


        @Override
        public void run() {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            flag = true;
        }
    }

    /**
     * 根据传入的参数进行处理，传入为0则表示游戏结束，传入为1表示开始游戏，传入2是倒时器开始
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    sumTime.interrupt();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
                    dialog.setTitle("游戏结束");
                    dialog.setMessage("本轮地鼠总数为： " + sumMouse + " 只 \n" +
                            "您逮住的地鼠共：" + clickMouse + " 只\n" +
                            "捕获率：" + clickMouse * 100 / sumMouse + "%"
                    );
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("再试一次", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            times.setText(60 + "");
                            t = 60;
                            sumMouse = 0;
                            clickMouse = 0;
                            onRestart();
                        }
                    });
                    dialog.show();
                    break;
                case 1:
                    Random random = new Random();
                    int index = random.nextInt(9);
                    ll_bg_show.setBackgroundResource(bgAtrr[index]);
                    sumMouse++;
                    if (btnAtrr[index] == mouseCheckedId) {
                        clickMouse++;  //判断当前老鼠所在的RadioButton的id与用户点击的是否一致，若一致则为打中地鼠
                    }
                    break;
                case 2:
                    times.setText(t + "");
                    t--;
                    break;
            }
        }
    }


    class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mouseCheckedId = v.getId();
            switch (v.getId()) {
                case R.id.bt_play:
                    if (flag) {
                        sumTime = new SumTime();
                        sumTime.start();
                        gameTime = new GameTime();
                        gameTime.start();
                        goTime = new GoTime();
                        goTime.start();
                    }
                    flag = false;
                    clickTime = new ClickTime();
                    new Thread(clickTime).start();
                    break;
                case R.id.bt_over:
                    finish();
                    Intent intent = new Intent(GameActivity.this, MyMusicService.class);
                    stopService(intent);
                    break;
                case R.id.cb_sound:
                    judgeMusic();
                    break;
            }
        }
    }

    private void initListener() {
        ButtonListener listener = new ButtonListener();
        playGame.setOnClickListener(listener);
        overGame.setOnClickListener(listener);

        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);
        btn5.setOnClickListener(listener);
        btn6.setOnClickListener(listener);
        btn7.setOnClickListener(listener);
        btn8.setOnClickListener(listener);
        btn9.setOnClickListener(listener);

        cb_sound.setOnClickListener(listener);

    }

    private void initAtrr() {
        bgAtrr[0] = R.drawable.game_background_1;
        bgAtrr[1] = R.drawable.game_background_2;
        bgAtrr[2] = R.drawable.game_background_3;
        bgAtrr[3] = R.drawable.game_background_4;
        bgAtrr[4] = R.drawable.game_background_5;
        bgAtrr[5] = R.drawable.game_background_6;
        bgAtrr[6] = R.drawable.game_background_7;
        bgAtrr[7] = R.drawable.game_background_8;
        bgAtrr[8] = R.drawable.game_background_9;

        btnAtrr[0] = R.id.btn1;
        btnAtrr[1] = R.id.btn2;
        btnAtrr[2] = R.id.btn3;
        btnAtrr[3] = R.id.btn4;
        btnAtrr[4] = R.id.btn5;
        btnAtrr[5] = R.id.btn6;
        btnAtrr[6] = R.id.btn7;
        btnAtrr[7] = R.id.btn8;
        btnAtrr[8] = R.id.btn9;
    }

    private void initView() {
        times = (TextView) findViewById(R.id.tv_time);

        playGame = (Button) findViewById(R.id.bt_play);
        overGame = (Button) findViewById(R.id.bt_over);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn8 = (Button) findViewById(R.id.btn8);
        btn9 = (Button) findViewById(R.id.btn9);

        ll_bg_show = (LinearLayout) findViewById(R.id.ll_bg_show);
        cb_sound = (CheckBox) findViewById(R.id.cb_sound);
    }
}
