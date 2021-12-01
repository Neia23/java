import java.io.*;

class BoardTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ConsoleBoard board = new ConsoleBoard();

		while(true)
		{
			board.print();
			System.out.print("黒石" + board.countDisc(Disc.BLACK) + " ");
			System.out.print("白石" + board.countDisc(Disc.WHITE) + " ");
			System.out.println("空マス" + board.countDisc(Disc.EMPTY));
			System.out.println();

			System.out.print("手を入力してください: ");
			Point p;
			String in;

			try
			{
				in = br.readLine();
			}
			catch(IOException e)
			{
				return;
			}
			
			if(in.equals("p"))
			{
				if(!board.pass())
				{
					System.out.println("パスできません");
				}
				continue;
			}
			
			if(in.equals("u"))
			{
				board.undo();
				continue;
			}

			try
			{
				p = new Point(in);
			}
			catch(IllegalArgumentException e)
			{
				System.out.println("リバーシの形式の手を入力してください");
				continue;
			}
			
			if(board.move(p) == false)
			{
				System.out.println("そこには置けません");
				continue;
			}

			if(board.isGameOver())
			{
				System.out.println("----------------ゲーム終了----------------");
				return;
			}
		}
	}
}
