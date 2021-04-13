package application;
	
import java.net.URL;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Snake extends Application {
	
	// Variablen
	public enum Direction {UP,DOWN,LEFT,RIGHT;}
	
	private static Stage window;
	
	public static final int BLOCK_SIZE = 20;
	public static final int GAME_WIDTH = 30*BLOCK_SIZE;
	public static final int GAME_HEIGHT = 20*BLOCK_SIZE;
	
	private static double speed = 0.2;					//0,2 Sekunden
	private static boolean isEndless = false;			//ob man durch den Rand kann oder dagegen st�sst
	
	private Direction direction = Direction.RIGHT;		//Direction.Werte die im ENUM angegeben wurden
	private boolean moved = false;						//ob sich Schlange bewegt
	private boolean running = false;					//ob das Spiel l�uft
	
	private Timeline timeLine = new Timeline();			//f�r Animation der Schlange
	
	private ObservableList<Node> snake;					//Schlange
	
	private MediaPlayer mediaPlayer;					//Musik
	private Slider volumeSlider = new Slider();
	private Label volumeLabel = new Label("100%");
	
	private int score = 0;
	private Label scoreLabel = new Label("Score: " + score);
	
	private Label info = new Label("Dr�cke ESC \num das Spiel abzubrechen \nund SPACE \num das Spiel zu pausieren.");
	
	// Methode um StartScene zu erstellen
	private BorderPane createStartScene() {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5,5,5,5));
		Label gameTitle = new Label("",new ImageView(new Image(getClass().getResourceAsStream("/images/snake.png"))));		
		
		Button startButton = new Button("Spiel starten");
		startButton.setOnAction(e -> {
			Scene scene = new Scene(createGame());
			keyPressed(scene);
			window.setScene(scene);
			window.show();
			window.setResizable(false);
			
			startGame();
		});
		
		Label settings = new Label("Einstellungen");
		settings.setFont(Font.font("Arial",FontWeight.BOLD,15));
		
		Button gameSpeed = new Button("Level");						//�ndert speed variable --> Schnelligkeit der Schlange
		Label gameSpeedLabel = new Label("Einfach");
		gameSpeed.setOnAction(e -> {
			if(speed == 0.2) {
				Snake.speed = 0.15;
				gameSpeedLabel.setText("Mittel");
			} else if (speed == 0.15) {
				Snake.speed = 0.09;
				gameSpeedLabel.setText("Schwer");
			} else if (speed == 0.09) {
				Snake.speed = 0.2;
				gameSpeedLabel.setText("Einfach");
			}
		});
		
		CheckBox gameBorder = new CheckBox();
		Label gameBorderLabel = new Label("Spiel mit Rand");
		gameBorder.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
				if(gameBorder.isSelected()) {
					gameBorderLabel.setText("Spiel mit Rand");
					gameBorderLabel.setStyle("-fx-border-style: solid;" + "-fx-border-insets: -3;" + "-fx-border-radius: 10;");
					isEndless = false;
				} else {
					gameBorderLabel.setText("Spiel ohne Rand");
					gameBorderLabel.setStyle("-fx-border-style: none;");
					isEndless = true;
				}
			}
		});
		gameBorder.setSelected(true);
		
		Label musik = new Label("Musik");
		musik.setFont(Font.font("Arial",FontWeight.BOLD,15));

		
		Button muteButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/images/mute.png"))));
		muteButton.setOnAction(e -> {
			mediaPlayer.pause();
		});
		Button unMuteButton = new Button("",new ImageView(new Image(getClass().getResourceAsStream("/images/unmute.png"))));
		unMuteButton.setOnAction(e -> {
			mediaPlayer.play();
		});
	
		HBox hBox = new HBox(30);
		hBox.getChildren().addAll(muteButton,unMuteButton);
		hBox.setAlignment(Pos.CENTER);
		Separator sep = new Separator();
		
		//		volumeSlider.setValue(mediaPlayer.getVolume()*100);
		volumeSlider.setValue(100);
		volumeSlider.setShowTickMarks(true);
		volumeSlider.setMinorTickCount(0);
		volumeSlider.setMajorTickUnit(50);
		volumeSlider.setShowTickLabels(true);
		
		volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				volumeLabel.setText((int)volumeSlider.getValue()+" %");
			}
		});
		
		GridPane settingsPane = new GridPane();
		GridPane.setConstraints(gameSpeed,0,0);
		GridPane.setConstraints(gameSpeedLabel,1,0);
		GridPane.setConstraints(gameBorder,0,1);
		GridPane.setConstraints(gameBorderLabel,1,1);
		GridPane.setConstraints(musik,0,3);
		GridPane.setColumnSpan(musik,2);
		GridPane.setHalignment(musik,HPos.CENTER);
		GridPane.setConstraints(hBox,0,4);
		GridPane.setColumnSpan(hBox,2);
		GridPane.setConstraints(sep,0,2);
		GridPane.setColumnSpan(sep,2);
		GridPane.setMargin(sep,new Insets(10,0,6,0));
		GridPane.setConstraints(volumeSlider,1,5);
		GridPane.setConstraints(volumeLabel,0,5);
		settingsPane.setHgap(10);
		settingsPane.setVgap(10);
		settingsPane.getChildren().addAll(gameSpeed,gameSpeedLabel,gameBorder,gameBorderLabel,sep,musik,hBox,volumeSlider,volumeLabel);
		settingsPane.setAlignment(Pos.CENTER);
		ColumnConstraints column1Width = new ColumnConstraints(60);
		ColumnConstraints column2Width = new ColumnConstraints(90);
		settingsPane.getColumnConstraints().addAll(column1Width,column2Width);
		
		Button exitButton = new Button("Anwendung beenden");
		exitButton.setOnAction(e -> {
			Platform.exit();
		});
		
		VBox vBox = new VBox(15);
		vBox.setAlignment(Pos.CENTER);
		VBox.setMargin(settings,new Insets(5,0,0,0));
		vBox.getChildren().addAll(gameTitle,startButton,settings,settingsPane,exitButton);
		
		BorderPane.setMargin(vBox, new Insets(20));
		root.setCenter(vBox);
		
		Label lbHelp = new Label();
		ImageView iVhelp = new ImageView(new Image("/images/help.png"));
		iVhelp.setFitHeight(60);
		iVhelp.setFitWidth(60);
		lbHelp.setGraphic(iVhelp);
		lbHelp.setTooltip(new Tooltip("Regeln"));
		lbHelp.setOnMouseEntered(mEntered -> {
			lbHelp.setScaleX(1.5);
			lbHelp.setScaleY(1.5);
		});
		lbHelp.setOnMouseExited(mExited -> {
			lbHelp.setScaleX(1);
			lbHelp.setScaleY(1);
		});
		lbHelp.setOnMouseClicked(mClicked -> {
			Stage helpStage = new Stage();
			Pane pane = new Pane();
			Scene helpScene = new Scene(pane,250,300);
			helpStage.setScene(helpScene);
			Text helpText = new Text("\nRegeln:\n\nBei diesem Spiel m�sst ihr schnell sein. Ihr seid die Schlange im Spielfeld. Diese Schlange k�nnt ihr mit den Pfeiltasten steuern: rechts, links, oben  und unten. Ihr d�rft je nach euren Einstellungen den Rand des Spielfeldes durchqueren oder eure Schlange stirbt bei Ber�hrung. \n\nZiel ist es die Nahrung im Spielfeld aufzusammeln. Durch die Nahrung wird die Schlange nach und nach l�nger, was das Spiel nat�rlich schwieriger macht.\n\nAu�erdem habt ihr die Auswahl zwischen drei Schwierigkeitslevel, einfach, mittel und schwer.");
			helpText.setWrappingWidth(210);
			helpText.setTranslateX(10);
			ScrollBar scrollBar = new ScrollBar();
			scrollBar.setLayoutX(helpScene.getWidth()-scrollBar.getWidth());
			scrollBar.setOrientation(Orientation.VERTICAL);
			scrollBar.setPrefHeight(helpScene.getHeight());
			scrollBar.setMax(300);
			scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					helpText.setLayoutY(-arg2.doubleValue());
				}					
			});
		
			pane.getChildren().addAll(helpText,scrollBar);
			helpStage.setResizable(false);
			helpStage.show();
		});
		
		VBox vBox2 = new VBox (20);
		vBox2.setAlignment(Pos.CENTER);
		VBox.setMargin(info, new Insets(0,0,30,0));
		info.setTextAlignment(TextAlignment.CENTER);
		vBox2.getChildren().addAll(info,lbHelp);
		root.setRight(vBox2);
		
		return root;
	}
	
	//SpielScene erstellen
	private Pane createGame() {
		Pane root = new Pane();
		root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
		root.setStyle("-fx-background-image: url(/images/gras.png);"+"-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;"+"-fx-border-color: black;"+"-fx-border-style: solid;"+"-fx-border-width: 2;");
		
		//Schlange
		Group snakeBody = new Group();
		snake = snakeBody.getChildren();
		
		//Nahrung
		Rectangle food = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
		Image foodImage = new Image("/images/food.png");
		ImagePattern imgPat = new ImagePattern(foodImage);
		food.setFill(imgPat);
		
		createRandomFood(food);
		
		//Animation
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(speed),new EventHandler<ActionEvent>() {		//alles in Handle Methode wird speed- Sekunden lang aufgerufen (je nach schwierigkeit)
			@Override
			public void handle(ActionEvent arg0) {
				if(!running) {
					return;
				}
				boolean toRemove = snake.size()>1;
				
				Node tail;
				if(toRemove) {
					tail = snake.remove(snake.size()-1);
				} else {
					tail = snake.get(0);
				}
				
				double tailX = tail.getTranslateX();			//aktuelle Position
				double tailY = tail.getTranslateY();
				
				switch(direction) {
				case UP:
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
					break;
				case DOWN:
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
					break;
				case LEFT:
					tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
					break;
				case RIGHT:
					tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
					break;
				default:
					break;
				}
				
				moved = true;
				
				if(toRemove) {
					snake.add(0,tail);
				}
				
				//Kollision
				for(Node rect: snake) {
					if(rect != tail && tail.getTranslateX() == rect.getTranslateX() && tail.getTranslateY() == rect.getTranslateY()) {
						score = 0;
						scoreLabel.setText("Score: "+score);
						restartGame();
						break;
					}
				}
				
				//Mit oder ohne Rand
				if(isEndless) {
					gameIsEndless(tail,root);
				} else {
					gameIsNotEndless(tail,food);
				}
				
				//Essen einsammeln bzw Schlange wird l�nger
				if(tail.getTranslateX()==food.getTranslateX() && tail.getTranslateY()==food.getTranslateY()) {
					createRandomFood(food);
					score += 20;
					scoreLabel.setText("Score: "+score);
					
					Rectangle rectangle = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
					rectangle.setTranslateX(tailX);
					rectangle.setTranslateY(tailY);
					snake.add(rectangle);
				}
			}
		});
		
		timeLine.getKeyFrames().add(keyFrame);
		timeLine.setCycleCount(Timeline.INDEFINITE); 			//Animation l�uft unendlich
		
		scoreLabel.setFont(Font.font("Arial",30));
		scoreLabel.setTranslateX(GAME_WIDTH/2);
		
		root.getChildren().addAll(food,snakeBody,scoreLabel);
		
		return root;
	}
	
	//Random food spawn
	private void createRandomFood(Node food) {
		food.setTranslateX((int)(Math.random()*(GAME_WIDTH-BLOCK_SIZE))/BLOCK_SIZE*BLOCK_SIZE);
		food.setTranslateY((int)(Math.random()*(GAME_HEIGHT-BLOCK_SIZE))/BLOCK_SIZE*BLOCK_SIZE);
	}
	
	//unendlich (kein Rand)
	private void gameIsEndless(Node tail, Parent root) {
		root.setStyle("-fx-background-image: url(/images/gras.png);"+"-fx-background-size: 20 20;" + "-fx-background-repeat: repeat;");
		
		if(tail.getTranslateX()<0) {
			tail.setTranslateX(GAME_WIDTH - BLOCK_SIZE);
		}
		if(tail.getTranslateX()>= GAME_WIDTH) {
			tail.setTranslateX(0);
		}
		if(tail.getTranslateY()<0) {
			tail.setTranslateY(GAME_HEIGHT - BLOCK_SIZE);
		}
		if(tail.getTranslateY()>= GAME_HEIGHT) {
			tail.setTranslateY(0);
		}
	}
	
	//nicht unendlich (mit Rand)
	private void gameIsNotEndless(Node tail, Node food) {
		if(tail.getTranslateX()<0 || tail.getTranslateX()>= GAME_WIDTH || tail.getTranslateY()<0 || tail.getTranslateY()>= GAME_HEIGHT) {
			score = 0;
			scoreLabel.setText("Score: "+score);
			restartGame();
			createRandomFood(food);
		}
	}
	
	//startGame
	private void startGame() {
		Rectangle head = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
		snake.add(head);
		timeLine.play();
		running = true;
	}
	
	//restartGame
	private void restartGame() {
		stopGame();
		startGame();
	}
	
	//stopGame
	private void stopGame() {
		running = false;
		timeLine.stop();
		snake.clear();
	}
	
	//Tastaturinteraktion
	private void keyPressed(Scene scene) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if(!moved) {			//damit Nutzer nicht gleichzeitig nach oben und unten fahren kann
					return;
				}
				
				switch (arg0.getCode()) {
				case W:
				case UP:
					if(direction != Direction.DOWN) {
						direction = Direction.UP;
						break;
					}
				case S:
				case DOWN:
					if(direction != Direction.UP) {
						direction = Direction.DOWN;
						break;
					}
				case A:
				case LEFT:
					if(direction != Direction.RIGHT) {
						direction = Direction.LEFT;
						break;
					}
				case D:
				case RIGHT:
					if(direction != Direction.LEFT) {
						direction = Direction.RIGHT;
						break;
					}
				case SPACE:
					timeLine.pause();
					scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent arg0) {
							if(arg0.getCode() == KeyCode.SPACE) {
								timeLine.playFromStart();
								keyPressed(scene);
							} else if(arg0.getCode() == KeyCode.ESCAPE) {
								//window.setScene(new Scene(createStartScene(),GAME_WIDTH,GAME_HEIGHT));
								Platform.exit();
							}
						}
					});
					break;
					
				case ESCAPE:
					//window.setScene(new Scene(createStartScene(),GAME_WIDTH,GAME_HEIGHT));
					Platform.exit();
					break;
				default:
					break;
				}
				moved = false;
			}
		});
	}
	
	
	// Musik
	private void playMusic(String title) {
		String musicFile = title;
		URL fileUrl = getClass().getResource(musicFile);
		
		Media media = new Media(fileUrl.toString());
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
		//		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); 			//spielt die Musik unendlich ab
	}
	@Override
	public void init() throws Exception {
		String musicFile = "/music/snakeMusic.mp3";
		//		playMusic(musicFile);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {			
			Parent root = createStartScene();			// warum Parent?
			primaryStage.setResizable(false);
			primaryStage.setTitle("snake");
			window = primaryStage;
			window.setScene(new Scene(root, GAME_WIDTH,GAME_HEIGHT));
			primaryStage.show();			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
