package net.ddns.gingerpi.chessboardnet;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class ChessBoardAdapter extends BaseAdapter{
    private Context mContext;
    private int dimensions=64;  //a chessboard is 64 squares large
    private int[] squareContents=new int[dimensions];
    private int[] tileColor;  //an array of 2 colours: eg. black and white
    int tileSize;

    public ChessBoardAdapter(Context c,int color1,int color2,int tileSize){
        mContext=c;
        this.tileColor=new int[]{color1,color2};
        this.tileSize=tileSize;
        for(int i=0; i<dimensions; i++)
            squareContents[i]=-1;
        squareContents[0]=R.drawable.doodle_black_pawn;
    }

    @Override
    public int getCount() {
        return  dimensions;
    }

    @Override
    public Object getItem(int position) {
        return squareContents[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView square=new ImageView(mContext);
        if((position/8)%2==0)
            square.setBackgroundColor(tileColor[position%2]);
        else
            square.setBackgroundColor(tileColor[(position+1)%2]);
        if(squareContents[position]!=-1)
            square.setImageDrawable(mContext.getResources().getDrawable(squareContents[position], null));

        square.setLayoutParams(new ViewGroup.LayoutParams(tileSize,tileSize));
        square.setPadding(0,0,0,0);

        return square;
    }

    public OnItemClickListener getOnItemClickListener= new OnItemClickListener(){
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            // make and display the toast message here
            Context button_context = mContext;
            CharSequence b1_text = getItem(position).toString();
            int display_for = Toast.LENGTH_SHORT;
            Toast my_toast = Toast.makeText(button_context,b1_text,display_for);
            my_toast.show();
        }
    };
}
