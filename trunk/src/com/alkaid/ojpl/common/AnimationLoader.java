package com.alkaid.ojpl.common;

import com.alkaid.ojpl.R;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationLoader
{
  public static Animation alphain;
  public static Animation alphaout;
  public static Animation close;
  public static Animation inAnim;
  public static Animation leftin;
  public static Animation leftout;
  public static Animation open;
  public static Animation outAnim;
  public static Animation rightin;
  public static Animation rightout;

  public static void load(Context context)
  {
    inAnim = AnimationUtils.loadAnimation(context, R.anim.wave_scale);
    outAnim = AnimationUtils.loadAnimation(context, R.anim.wave_scale_out);
    leftout = AnimationUtils.loadAnimation(context, R.anim.step_leftin);
    leftin = AnimationUtils.loadAnimation(context, R.anim.step_leftout);
    rightin = AnimationUtils.loadAnimation(context, R.anim.step_rightin);
    rightout = AnimationUtils.loadAnimation(context, R.anim.step_rightout);
    open = AnimationUtils.loadAnimation(context, R.anim.open);
    close = AnimationUtils.loadAnimation(context, R.anim.close);
    alphain = new AlphaAnimation(0.0F, 1.0F);
    alphain.setDuration(300L);
    alphain.setFillAfter(true);
    alphaout = new AlphaAnimation(1.0F, 0.0F);
    alphaout.setDuration(300L);
    alphaout.setFillAfter(true);
  }
}

/* Location:           E:\SOFTWARE\Coder\Android\crack\projects\djry1\djry1.dex.dex2jar.jar
 * Qualified Name:     com.hj.djdry1.utils.AnimationLoader
 * JD-Core Version:    0.6.0
 */