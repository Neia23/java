import java.io.*;

interface Player
{
	public void onTurn(Board board) throws Exception;
}

class UndoException extends Exception
{
}

class ExitException extends Exception
{
}

class GameOverException extends Exception
{
}

class HumanPlayer implements Player
{
	public void onTurn(Board board) throws Exception
	{
		if(board.getMovablePos().isEmpty())
		{
			// パス
			System.out.println("あなたはパスです");
			board.pass();
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while(true)
		{
			System.out.print("手を\"f5\"のように入力、もしくは(U:取消し/X:終了)を入力してください:");
			String in = br.readLine();

			if(in.equalsIgnoreCase("U")) throw new UndoException();
			
			if(in.equalsIgnoreCase("X")) throw new ExitException();
			
			Point p;
			try{
				p = new Point(in);
			}
			catch(IllegalArgumentException e)
			{
				System.out.println("正しい形式で入力してください！");
				continue;
			}
			
			if(!board.move(p))
			{
				System.out.println("そこには置けません！");
				continue;
			}
			
			if(board.isGameOver()) throw new GameOverException();
			
			break;
		}
	}
}

class AIPlayer implements Player
{

	private AI Ai = null;

	public AIPlayer()
	{
		Ai = new AlphaBetaAI();
	}

	public void onTurn(Board board) throws GameOverException
	{
		System.out.print("コンピュータ思考中...");
		Ai.move(board);
		System.out.println("完了");
		if(board.isGameOver()) throw new GameOverException();
	}
}

class ReversiGame
{
	public static void main(String[] args)
	{
		Player[] player = new Player[2];
		int current_player = 0;
		ConsoleBoard board = new ConsoleBoard();
		boolean reverse = false;
		
		if(args.length > 0)
		{
			// コマンドラインオプション -r が与えられるとコンピュータ先手にする
			if(args[0].equals("-r")) reverse = true;
		}

		// 先手・後手の設定
		if(reverse)
		{
			player[0] = new AIPlayer();
			player[1] = new HumanPlayer();
		}
		else{
			player[0] = new HumanPlayer();
			player[1] = new AIPlayer();
		}

		while(true)
		{
			board.print();

			try{
				player[current_player].onTurn(board);
			}
			catch(UndoException e)
			{
				do
				{
					board.undo();
					board.undo();
				}
				while(board.getMovablePos().isEmpty());
				continue;
			}
			catch(ExitException e){
				return;
			}
			catch(GameOverException e)
			{
				System.out.println("ゲーム終了");
				System.out.print("黒石" + board.countDisc(Disc.BLACK) + " ");
				System.out.println("白石" + board.countDisc(Disc.WHITE));
				return;
			}
			catch(Exception e)
			{
				// 予期しない例外
				System.out.println("Unexpected exception: " + e);
				return;
			}
			// プレイヤーの交代
			current_player = ++current_player % 2;
		}
	}
}
