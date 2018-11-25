package net.ddns.gingerpi.chessboardnet;

import static net.ddns.gingerpi.chessboardnetCommon.ChessBoard.texturePack;
import static net.ddns.gingerpi.chessboardnet.ChessSet.piece.*;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
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
    private ChessSet p1set;    //icons for player 1's chess set
    private ChessSet p2set;    //icons for player 2's chess set
    int tileSize;

    public ChessBoardAdapter(Context c,int color1,int color2,int tileSize,texturePack set1,texturePack set2){
        mContext=c;
        this.tileColor=new int[]{color1,color2};
        this.tileSize=tileSize;
        for(int i=0; i<dimensions; i++)
            squareContents[i]=-1;
        p1set=new ChessSet(mContext, set1);
        p2set=new ChessSet(mContext, set2);
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

            //squareContents[position]=p1set.getPiece(ChessSet.piece.rook_front);
            //notifyDataSetChanged();
        }
    };

    public void refreshBoard(String contents){
        for(int i=0; i<dimensions; i++){
            switch(contents.charAt(i)){
                case 'K':{
                    squareContents[i]=p2set.getPiece(king_front);
                    break;
                }

                case 'k':{
                    squareContents[i]=p1set.getPiece(king_back);
                    break;
                }

                case 'Q':{
                    squareContents[i]=p2set.getPiece(queen_front);
                    break;
                }

                case 'q':{
                    squareContents[i]=p1set.getPiece(queen_back);
                    break;
                }

                case 'B':{
                    squareContents[i]=p2set.getPiece(bishop_front);
                    break;
                }

                case 'b':{
                    squareContents[i]=p1set.getPiece(bishop_back);
                    break;
                }

                case 'N':{
                    squareContents[i]=p2set.getPiece(knight_front);
                    break;
                }

                case 'n':{
                    squareContents[i]=p1set.getPiece(knight_back);
                    break;
                }

                case 'R':{
                    squareContents[i]=p2set.getPiece(rook_front);
                    break;
                }

                case 'r':{
                    squareContents[i]=p1set.getPiece(rook_back);
                    break;
                }

                case 'P':{
                    squareContents[i]=p2set.getPiece(pawn_front);
                    break;
                }

                case 'p':{
                    squareContents[i]=p1set.getPiece(pawn_back);
                    break;
                }

                default: {
                    squareContents[i]=-1;
                }
            }
        }

        notifyDataSetChanged();
    }
}
