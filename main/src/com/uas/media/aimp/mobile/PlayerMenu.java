/*
 * Copyright (c) 2013, Sorokin Alexander (uas.sorokin@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the authors may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.uas.media.aimp.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.uas.media.aimp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public class PlayerMenu {

	public static final int TYPE_CONNECT = 1;
	public static final int TYPE_DISCONNECT = 2;
	public static final int TYPE_INFO = 3;
	public static final int TYPE_PREFERENCES = 4;
	public static final int TYPE_EXIT = 5;
	public static final int TYPE_DONATE = 6;

	public interface OnChooseListener {
		void onChoose(int type);
	}


	private PlayerMenu() {
		
	}

	public static AlertDialog createDialog(Context ctx, int[] items, final OnChooseListener listener) {
    	final MenuListAdapter itemsAdapter = new MenuListAdapter(ctx, buildItems(ctx, items));
    	return new AlertDialog.Builder(ctx)
			.setTitle(R.string.menu_dialog_title)
			.setAdapter(itemsAdapter, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	if (listener != null) {
			    		listener.onChoose (itemsAdapter.getItem(item).getType());
			    	}
			    }
			})
			.setNeutralButton(R.string.menu_dialog_close, null)
            .setCancelable(true)
			.create()
		;
	}


	private static List<PlayerMenuItem> buildItems(Context ctx, int[] types) {
     	List<PlayerMenuItem> items = new ArrayList<PlayerMenuItem>();
     	for (int type: types) {
     		items.add(getMenuItem(ctx, type));
     	}
     	return items;
	}
	
	
	private static PlayerMenuItem getMenuItem(Context ctx, int type) {
		PlayerMenuItem item = null;

     	if (type == TYPE_CONNECT) {
        	item = new PlayerMenuItem(
        			TYPE_CONNECT,
        			ctx.getResources().getDrawable(R.drawable.ic_network_connecting_small),
        			ctx.getString(R.string.menu_dialog_connect)
        	);
     	}
     	if (type == TYPE_DISCONNECT) {
        	item = new PlayerMenuItem(
        			TYPE_DISCONNECT,
        			ctx.getResources().getDrawable(R.drawable.ic_network_disconnected_small),
        			ctx.getString(R.string.menu_dialog_disconnect)
        	);
     	}

     	if (type == TYPE_INFO) {
        	item = new PlayerMenuItem(
        			TYPE_INFO,
        			ctx.getResources().getDrawable(R.drawable.ic_info),
        			ctx.getString(R.string.menu_dialog_info)
        	);
     	}
     	if (type == TYPE_PREFERENCES) {
        	item = new PlayerMenuItem(
        			TYPE_PREFERENCES,
        			ctx.getResources().getDrawable(R.drawable.ic_settings),
        			ctx.getString(R.string.menu_dialog_settings)
        	);
     	}

     	if (type == TYPE_DONATE) {
        	item = new PlayerMenuItem(
        			TYPE_DONATE,
        			ctx.getResources().getDrawable(R.drawable.ic_donate),
        			ctx.getString(R.string.menu_dialog_donate)
        	);
     	}
     	if (type == TYPE_EXIT) {
        	item = new PlayerMenuItem(
        			TYPE_EXIT,
        			ctx.getResources().getDrawable(R.drawable.ic_menu_exit),
        			ctx.getString(R.string.menu_dialog_exit)
        	);
     	}

     	return item;
	}
	

    static class PlayerMenuItem {

    	private final int mType;
    	private final Drawable mImage;
    	private final String mLabel;

    	public PlayerMenuItem(int type, Drawable image, String label) {
    		mType = type;
    		mImage = image;
    		mLabel = label;
    	}

    	public int getType() {
    		return mType;
    	}

    	public Drawable getImage() {
    		return mImage;
    	}

    	public String getLabel() {
    		return mLabel;
    	}
    }


	static class MenuListAdapter extends ArrayAdapter<PlayerMenuItem> {

		private final LayoutInflater mLayoutInflater;
		private List<PlayerMenuItem> mList;


		public MenuListAdapter(Context context, List<PlayerMenuItem> objects) {
			super(context, -1, objects);
			mLayoutInflater = LayoutInflater.from(context);
			setList(objects);
		}

		public void setList(List<PlayerMenuItem> objects) {
			mList = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = mLayoutInflater.inflate(R.layout.player_menu_item, null);
            }

            final PlayerMenuItem mi = mList.get(position);
            if (mi != null) {
            	final ImageView ivImage = (ImageView) v.findViewById(R.id.player_menu_item_image);
            	final TextView tvLabel = (TextView) v.findViewById(R.id.player_menu_item_label);

            	ivImage.setImageDrawable(mi.getImage());
            	tvLabel.setText(mi.getLabel());
            }
            return v;
		}

	}

}