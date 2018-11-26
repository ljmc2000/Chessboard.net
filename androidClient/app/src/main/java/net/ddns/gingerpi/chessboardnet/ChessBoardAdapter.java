package net.ddns.gingerpi.chessboardnet;

import static net.ddns.gingerpi.chessboardnetCommon.ChessBoard.texturePack;
import static net.ddns.gingerpi.chessboardnet.ChessSet.piece.*;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import net.ddns.gingerpi.chessboardnetCommon.ChessBoard;
import net.ddns.gingerpi.chessboardnetCommon.ChessPiece;

import java.util.ArrayList;

public class ChessBoardAdapter extends BaseAdapter{
    private Context mContext;
    ChessBoard chessBoard;
    private int dimensions=64;  //a chessboard is 64 squares large
    private int[] squareContents=new int[dimensions];
    private ChessSet p1set;    //icons for player 1's chess set
    private ChessSet p2set;    //icons for player 2's chess set
    boolean color;
    int tileSize;
    int selectedSquare=-1;

    public ChessBoardAdapter(Context c,int tileSize,texturePack set1,texturePack set2){
        mContext=c;
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

        if(squareContents[position]==-2) {
            if (position %2 == (position/8)%2)
                square.setBackgroundColor(mContext.getColor(R.color.chessTileLightLit));
            else
                square.setBackgroundColor(mContext.getColor(R.color.chessTileDarkLit));
        }

        else{
            if (position %2 == (position/8)%2)
                square.setBackgroundColor(mContext.getColor(R.color.chessTileLight));
            else
                square.setBackgroundColor(mContext.getColor(R.color.chessTileDark));
        }

        if(squareContents[position]>-1)
            square.setImageDrawable(mContext.getResources().getDrawable(squareContents[position], null));

        square.setLayoutParams(new ViewGroup.LayoutParams(tileSize,tileSize));
        square.setPadding(0,0,0,0);

        return square;
    }

    public OnItemClickListener getOnItemClickListener= new OnItemClickListener(){
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            if(chessBoard==null)
                return;

            else if(selectedSquare!=-1) {
                selectedSquare = -1;
                for(int i=0; i<dimensions; i++){
                    if(squareContents[i]==-2) squareContents[i]=-1;
                }
            }

            else{
                selectedSquare=position;

                ChessPiece p=chessBoard.getItem(position);
                if(p==null) {
                    selectedSquare = -1;
                    return;
                }

                ArrayList<Integer> validMoves=p.getLegalMoves(position, chessBoard);
                int square;
                for (int i = 0; i < validMoves.size(); i++) {
                    square=validMoves.get(i);
                    if(0<square && square<64)
                    if(squareContents[square]==-1)
                        squareContents[square]=-2;
                }

                if(validMoves.size()==0)
                    selectedSquare=-1;
            }

            notifyDataSetChanged();
        }
    };

    public void refreshBoard(String contents){
        for(int i=0; i<dimensions; i++){
            if(color)
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


            else
            switch(contents.charAt(i)){
                case 'K':{
                    squareContents[i]=p2set.getPiece(king_back);
                    break;
                }

                case 'k':{
                    squareContents[i]=p1set.getPiece(king_front);
                    break;
                }

                case 'Q':{
                    squareContents[i]=p2set.getPiece(queen_back);
                    break;
                }

                case 'q':{
                    squareContents[i]=p1set.getPiece(queen_front);
                    break;
                }

                case 'B':{
                    squareContents[i]=p2set.getPiece(bishop_back);
                    break;
                }

                case 'b':{
                    squareContents[i]=p1set.getPiece(bishop_front);
                    break;
                }

                case 'N':{
                    squareContents[i]=p2set.getPiece(knight_back);
                    break;
                }

                case 'n':{
                    squareContents[i]=p1set.getPiece(knight_front);
                    break;
                }

                case 'R':{
                    squareContents[i]=p2set.getPiece(rook_back);
                    break;
                }

                case 'r':{
                    squareContents[i]=p1set.getPiece(rook_front);
                    break;
                }

                case 'P':{
                    squareContents[i]=p2set.getPiece(pawn_back);
                    break;
                }

                case 'p':{
                    squareContents[i]=p1set.getPiece(pawn_front);
                    break;
                }

                default: {
                    squareContents[i]=-1;
                }
            }


        }

        notifyDataSetChanged();
    }

    public void setChessBoard(ChessBoard chessBoard){
        this.chessBoard=chessBoard;
    }

    public void setColor(boolean color){
        this.color=color;
    }
}
