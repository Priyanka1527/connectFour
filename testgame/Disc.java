package testgame;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Disc extends Circle {
	public final boolean yellow;
	public Disc(boolean yellow) {
		super(Connect4App.TILE_SIZE / 2, yellow ? Color.YELLOW : Color.HOTPINK);
		this.yellow = yellow;

		setCenterX(Connect4App.TILE_SIZE / 2);
		setCenterY(Connect4App.TILE_SIZE / 2);
	}
	}
