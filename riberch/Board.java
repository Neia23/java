import java.util.*;

class Board {
	public static final int BOARD_SIZE = 8;
	public static final int MAX_TURNS = 60;

	private static final int NONE = 0;
	private static final int UPPER = 1;
	private static final int UPPER_LEFT = 2;
	private static final int LEFT = 4;
	private static final int LOWER_LEFT = 8;
	private static final int LOWER = 16;
	private static final int LOWER_RIGHT = 32;
	private static final int RIGHT = 64;
	private static final int UPPER_RIGHT = 128;

	private int RawBoard[][] = new int[BOARD_SIZE + 2][BOARD_SIZE + 2];
	private int Turns; // 手数(0からはじまる)
	private int CurrentColor; // 現在のプレイヤー

	private Vector UpdateLog = new Vector();

	private Vector MovablePos[] = new Vector[MAX_TURNS + 1];
	private int MovableDir[][][] = new int[MAX_TURNS + 1][BOARD_SIZE + 2][BOARD_SIZE + 2];// [手数][縦座標][横座標]

	private ColorStorage Discs = new ColorStorage();

	public Board() {
		// Vectorの配列を初期化
		for (int i = 0; i <= MAX_TURNS; i++) {
			MovablePos[i] = new Vector();
		}

		init();
	}

	public void init() {
		// 全マスを空きマスに設定
		for (int x = 1; x <= BOARD_SIZE; x++) {
			for (int y = 1; y <= BOARD_SIZE; y++) {
				RawBoard[x][y] = Disc.EMPTY;
			}
		}

		// 壁の設定
		for (int y = 0; y < BOARD_SIZE + 2; y++) {
			RawBoard[0][y] = Disc.WALL;
			RawBoard[BOARD_SIZE + 1][y] = Disc.WALL;
		}

		for (int x = 0; x < BOARD_SIZE + 2; x++) {
			RawBoard[x][0] = Disc.WALL;
			RawBoard[x][BOARD_SIZE + 1] = Disc.WALL;
		}

		// 初期設定
		RawBoard[4][4] = Disc.WHITE;
		RawBoard[5][5] = Disc.WHITE;
		RawBoard[4][5] = Disc.BLACK;
		RawBoard[5][4] = Disc.BLACK;

		// 石数の初期設定
		Discs.set(Disc.BLACK, 2);
		Discs.set(Disc.WHITE, 2);
		Discs.set(Disc.EMPTY, BOARD_SIZE * BOARD_SIZE - 4);

		Turns = 0; // 手数は０から数える
		CurrentColor = Disc.BLACK; // 先手は黒

		initMovable();
	}

	public boolean move(Point point) {
		if (point.x <= 0 || point.x > BOARD_SIZE)
			return false;
		if (point.y <= 0 || point.y > BOARD_SIZE)
			return false;
		if (MovableDir[Turns][point.x][point.y] == NONE)
			return false;

		flipDiscs(point);

		Turns++;
		CurrentColor = -CurrentColor;

		initMovable();

		return true;
	}

	// 直前の一手を元に戻す。成功した＝true 元に戻せないまたはまだ一手も打っていない場合=false
	public boolean undo() {
		// ゲーム開始地点の場合は戻れない
		if (Turns == 0)
			return false;

		CurrentColor = -CurrentColor;

		Vector update = (Vector) UpdateLog.remove(UpdateLog.size() - 1);

		// 前回がパスであるかどうかで処理分け
		if (update.isEmpty()) {
			// 前回はパス

			// MovablePosとMovableDirを再構築
			MovablePos[Turns].clear();
			for (int x = 1; x <= BOARD_SIZE; x++) {
				for (int y = 1; y <= BOARD_SIZE; y++) {
					MovableDir[Turns][x][y] = NONE;
				}
			}
		} else {
			// 前回はパスできない

			Turns--;

			// 石を元に戻す
			Point p = (Point) update.get(0);
			RawBoard[p.x][p.y] = Disc.EMPTY;
			for (int i = 1; i < update.size(); i++) {
				p = (Point) update.get(i);
				RawBoard[p.x][p.y] = -CurrentColor;
			}

			// 石数の更新
			int discdiff = update.size();
			Discs.set(CurrentColor, Discs.get(CurrentColor) - discdiff);
			Discs.set(-CurrentColor, Discs.get(-CurrentColor) + (discdiff - 1));
			Discs.set(Disc.EMPTY, Discs.get(Disc.EMPTY) + 1);
		}

		return true;
	}

	public boolean pass() {
		// 打つ手があるなら、パスはできない
		if (MovablePos[Turns].size() != 0)
			return false;

		// ゲームが終了しているなら、パスはできない
		if (isGameOver())
			return false;

		CurrentColor = -CurrentColor;

		UpdateLog.add(new Vector());

		initMovable();

		return true;

	}

	// pointで指定された位置の色を返す
	public int getColor(Point point) {
		return RawBoard[point.x][point.y];
	}

	// 現在の手番の色を返す。
	public int getCurrentColor() {
		return CurrentColor;
	}

	// 現在の手数を返す。初期値は０
	public int getTurns() {
		return Turns;
	}

	public boolean isGameOver() {
		// 60手に達していたらゲーム終了
		if (Turns == MAX_TURNS)
			return true;

		// 打てる手があるならゲーム終了ではない
		if (MovablePos[Turns].size() != 0)
			return false;

		//
		// 現在の手番と逆の色が打てるかを調べる
		//
		Disc disc = new Disc();
		disc.color = -CurrentColor;
		for (int x = 1; x <= BOARD_SIZE; x++) {
			disc.x = x;
			for (int y = 1; y <= BOARD_SIZE; y++) {
				disc.y = y;
				// 置ける箇所が１つでもある場合はゲーム継続
				if (checkMobility(disc) != NONE)
					return false;
			}
		}

		return true;
	}

	// colorで指定された色の石の数を数える。色にはBLACK、WHITE、EMPTYを指定可能
	public int countDisc(int color) {
		return Discs.get(color);
	}

	// 石を打てる座標が並んだvectorを返す。
	public Vector getMovablePos() {
		return MovablePos[Turns];
	}

	public Vector getHistory() {
		Vector history = new Vector();

		for (int i = 0; i < UpdateLog.size(); i++) {
			Vector update = (Vector) UpdateLog.get(i);
			if (update.isEmpty())
				continue; // パスは飛ばす
			history.add(update.get(0));
		}

		return history;
	}

	// 直前の手で打った石と裏返した石が並んだvectorを返す。
	public Vector getUpdate() {
		if (UpdateLog.isEmpty())
			return new Vector();
		else
			return (Vector) UpdateLog.lastElement();
	}

	public int getLiberty(Point p) {
		// ��
		return 0;
	}

	private int checkMobility(Disc disc) {
		// すでに石があったら置けない
		if (RawBoard[disc.x][disc.y] != Disc.EMPTY)
			return NONE;

		int x, y;
		int dir = NONE;

		// 上
		if (RawBoard[disc.x][disc.y - 1] == -disc.color) {
			x = disc.x;
			y = disc.y - 2;
			while (RawBoard[x][y] == -disc.color) {
				y--;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= UPPER;
		}

		// 下
		if (RawBoard[disc.x][disc.y + 1] == -disc.color) {
			x = disc.x;
			y = disc.y + 2;
			while (RawBoard[x][y] == -disc.color) {
				y++;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= LOWER;
		}

		// 左
		if (RawBoard[disc.x - 1][disc.y] == -disc.color) {
			x = disc.x - 2;
			y = disc.y;
			while (RawBoard[x][y] == -disc.color) {
				x--;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= LEFT;
		}

		// 右
		if (RawBoard[disc.x + 1][disc.y] == -disc.color) {
			x = disc.x + 2;
			y = disc.y;
			while (RawBoard[x][y] == -disc.color) {
				x++;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= RIGHT;
		}

		// 右上
		if (RawBoard[disc.x + 1][disc.y - 1] == -disc.color) {
			x = disc.x + 2;
			y = disc.y - 2;
			while (RawBoard[x][y] == -disc.color) {
				x++;
				y--;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= UPPER_RIGHT;
		}

		// 左上
		if (RawBoard[disc.x - 1][disc.y - 1] == -disc.color) {
			x = disc.x - 2;
			y = disc.y - 2;
			while (RawBoard[x][y] == -disc.color) {
				x--;
				y--;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= UPPER_LEFT;
		}

		// 左下
		if (RawBoard[disc.x - 1][disc.y + 1] == -disc.color) {
			x = disc.x - 2;
			y = disc.y + 2;
			while (RawBoard[x][y] == -disc.color) {
				x--;
				y++;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= LOWER_LEFT;
		}

		// 右下
		if (RawBoard[disc.x + 1][disc.y + 1] == -disc.color) {
			x = disc.x + 2;
			y = disc.y + 2;
			while (RawBoard[x][y] == -disc.color) {
				x++;
				y++;
			}
			if (RawBoard[x][y] == disc.color)
				dir |= LOWER_RIGHT;
		}

		return dir;
	}

	// MovablePos[Terns]とMovableDir[Terns]を再計算する
	private void initMovable() {
		Disc disc;
		int dir;

		MovablePos[Turns].clear();

		for (int x = 1; x <= BOARD_SIZE; x++) {
			for (int y = 1; y <= BOARD_SIZE; y++) {
				disc = new Disc(x, y, CurrentColor);
				dir = checkMobility(disc);
				if (dir != NONE) {
					// 置ける場所
					MovablePos[Turns].add(disc);
				}
				MovableDir[Turns][x][y] = dir;
			}
		}
	}

	private void flipDiscs(Point point) {
		int x, y;
		int dir = MovableDir[Turns][point.x][point.y];

		Vector update = new Vector();

		RawBoard[point.x][point.y] = CurrentColor;
		update.add(new Disc(point.x, point.y, CurrentColor));

		// 上

		if ((dir & UPPER) != NONE) // 上に置ける場合
		{
			y = point.y;
			while (RawBoard[point.x][--y] != CurrentColor) {
				RawBoard[point.x][y] = CurrentColor;
				update.add(new Disc(point.x, y, CurrentColor));
			}
		}

		// 下

		if ((dir & LOWER) != NONE) {
			y = point.y;
			while (RawBoard[point.x][++y] != CurrentColor) {
				RawBoard[point.x][y] = CurrentColor;
				update.add(new Disc(point.x, y, CurrentColor));
			}
		}

		// 左

		if ((dir & LEFT) != NONE) {
			x = point.x;
			while (RawBoard[--x][point.y] != CurrentColor) {
				RawBoard[x][point.y] = CurrentColor;
				update.add(new Disc(x, point.y, CurrentColor));
			}
		}

		// 右

		if ((dir & RIGHT) != NONE) {
			x = point.x;
			while (RawBoard[++x][point.y] != CurrentColor) {
				RawBoard[x][point.y] = CurrentColor;
				update.add(new Disc(x, point.y, CurrentColor));
			}
		}

		// 右上

		if ((dir & UPPER_RIGHT) != NONE) {
			x = point.x;
			y = point.y;
			while (RawBoard[++x][--y] != CurrentColor) {
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 左上

		if ((dir & UPPER_LEFT) != NONE) {
			x = point.x;
			y = point.y;
			while (RawBoard[--x][--y] != CurrentColor) {
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 左下

		if ((dir & LOWER_LEFT) != NONE) {
			x = point.x;
			y = point.y;
			while (RawBoard[--x][++y] != CurrentColor) {
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 右下

		if ((dir & LOWER_RIGHT) != NONE) {
			x = point.x;
			y = point.y;
			while (RawBoard[++x][++y] != CurrentColor) {
				RawBoard[x][y] = CurrentColor;
				update.add(new Disc(x, y, CurrentColor));
			}
		}

		// 石の数を更新

		int discdiff = update.size();

		Discs.set(CurrentColor, Discs.get(CurrentColor) + discdiff);
		Discs.set(-CurrentColor, Discs.get(-CurrentColor) - (discdiff - 1));
		Discs.set(Disc.EMPTY, Discs.get(Disc.EMPTY) - 1);

		UpdateLog.add(update);
	}

}
