package com.sin.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * A Arrow View, default direction is left, you can set direction by set tag,
 * tag can be set as left, right, up or down.
 * 
 * @author RobinTang
 * @date 2013-7-10
 */
public class ArrowView extends View {
	private Path path = new Path();
	private Paint paint = new Paint();
	private ArrowDirection direction = ArrowDirection.left;
	private int color = Color.BLACK;

	public enum ArrowDirection {
		left, up, right, down,
	}

	public ArrowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initThis();
	}

	public ArrowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initThis();
	}

	public ArrowView(Context context) {
		super(context);
		initThis();
	}

	private void initThis() {
		paint.setColor(color);
		paint.setStyle(Paint.Style.FILL);

		Object tag = getTag();
		if (tag != null) {
			String s = tag.toString();
			if (s.equals("up")) {
				direction = ArrowDirection.up;
			} else if (s.equals("right")) {
				direction = ArrowDirection.right;
			} else if (s.equals("down")) {
				direction = ArrowDirection.down;
			} else {
				direction = ArrowDirection.left;
			}
		}
	}

	public ArrowDirection getDirection() {
		return direction;
	}

	public void setDirection(ArrowDirection direction) {
		this.direction = direction;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		this.paint.setColor(color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int vw = getWidth();
		int vh = getHeight();
		int aw;
		int pw;
		int am;
		switch (this.direction) {
		case right:
			// ->
			aw = vh / 3;
			pw = vh;
			am = (vh - aw) / 2;
			path.moveTo(0, am);
			path.lineTo(vw - pw, am);
			path.lineTo(vw - pw, 0);
			path.lineTo(vw, vh / 2);
			path.lineTo(vw - pw, vh);
			path.lineTo(vw - pw, vh - am);
			path.lineTo(0, vh - am);
			break;

		case left:
			// ->
			aw = vh / 3;
			pw = vh;
			am = (vh - aw) / 2;
			path.moveTo(vw, am);
			path.lineTo(pw, am);
			path.lineTo(pw, 0);
			path.lineTo(0, vh / 2);
			path.lineTo(pw, vh);
			path.lineTo(pw, vh - am);
			path.lineTo(vw, vh - am);
			break;
		case up:
			// ->
			aw = vw / 3;
			pw = vw;
			am = (vw - aw) / 2;
			path.moveTo(am, vh);
			path.lineTo(am, pw);
			path.lineTo(0, pw);
			path.lineTo(vw / 2, 0);
			path.lineTo(vw, pw);
			path.lineTo(vw - am, pw);
			path.lineTo(vw - am, vh);
			break;
		case down:
			// ->
			aw = vw / 3;
			pw = vw;
			am = (vw - aw) / 2;
			path.moveTo(am, 0);
			path.lineTo(am, vh - pw);
			path.lineTo(0, vh - pw);
			path.lineTo(vw / 2, vh);
			path.lineTo(vw, vh - pw);
			path.lineTo(vw - am, vh - pw);
			path.lineTo(vw - am, 0);
			break;
		default:
			break;
		}

		path.close();
		canvas.drawPath(path, paint);
	}
}
