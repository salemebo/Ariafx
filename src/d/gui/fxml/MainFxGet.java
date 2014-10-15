package d.gui.fxml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.commons.io.FileUtils;

import d.core.download.Download;
import d.core.download.ItemReady;
import d.core.download.Link;
import d.core.url.Item;
import d.core.url.Url;
import d.core.url.type.DownState;
import d.core.url.type.GState;
import d.core.url.type.Type;
import d.gui.fxml.control.AddCategory;
import d.gui.fxml.control.AddQueue;
import d.gui.fxml.control.FeatchURL;
import d.gui.manager.DownList;
import d.opt.R;

public class MainFxGet implements Initializable {

	public static URL FXML;
	public Stage stage;

	public static boolean isMinimal = false;
	public MainFxGet(Stage stage) {
		if (FXML == null)
			FXML = getClass().getResource("mainFxGet.xml");
		this.stage = stage;
	}

	@FXML
	public HBox hBoxHeader, hBoxFind;

	@FXML
	public ListView<AnchorPane> list;

	@FXML
	public TextField findField;

	@FXML
	public TreeView<Type> treeView;

	@FXML
	public TreeView<GState> treeState;

	@FXML
	public Label labelMessages;

	@FXML
	public ContextMenu contxtOpt, contextMenuTree;

	@FXML
	public Button btnOpt, deletButton, deletCompButton;

	@FXML
	public ImageView findIcon;

	@FXML
	public AnchorPane anchorRoot;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		initTreeView();
		initSelectTreeProperty();


		initTranslateTransition();
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		list.setItems(DownList.AnchorList);
		findField.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					if (newValue.equals(oldValue))
						return;
					if (newValue.equals("") || newValue == null) {
						findIcon.getStyleClass().removeAll("image-close",
								"image-find");
						findIcon.getStyleClass().add("image-find");
						// init list to default
						list.setItems(DownList.AnchorList);
					} else {
						findIcon.getStyleClass().removeAll("image-close",
								"image-find");
						findIcon.getStyleClass().add("image-close");
						// init searched list
						ObservableList<Link> down = DownList
								.findByTitle(newValue);
						list.setItems(DownList.getAnchor(down));

					}
				});

		findIcon.setOnMouseClicked((e) -> {
			findField.setText("");
		});
		
	}
	
	Transition transition;
	boolean goLeft = true;
	private void initTranslateTransition() {
		
		transition = new Transition() {
			double xStart = list.getLayoutX();
			double xEnd   = treeState.getLayoutX() -1;
			{
				setCycleDuration(Duration.millis(750));
			}
			
			@Override
			protected void interpolate(double frac) {
				if (!Double.isNaN(frac)) {
					double value ;
					if(goLeft){
						value = xStart-(xStart - xEnd)*frac;
						list.setLayoutX(value);
						
					}else{
						value = xEnd + (xStart-xEnd)*frac;
						list.setLayoutX(value);
					}
					anchorRoot.setPrefWidth(value + list.getWidth() + xEnd);
					stage.sizeToScene();
				}
			}
		};
		
		transition.setOnFinished((e)->{goLeft = !goLeft;});
		
	}

	public static TreeItem<Type> category, queue;
	
	@SuppressWarnings("unchecked")
	public void initTreeView() {

		// R.LoadTreeItems();

		category = new TreeItem<>(new Type("Downloads"));
		category.setExpanded(true);
		category.getChildren().addAll(Type.categoryList);

		queue = new TreeItem<>(new Type("Queues"));
		queue.setExpanded(true);
		queue.getChildren().addAll(Type.queueList);

		TreeItem<Type> root = new TreeItem<>();
		root.setExpanded(true);
		root.getChildren().addAll(category, queue);

		if (treeView.getRoot() != null)
			treeView.getRoot().getChildren().clear();
		treeView.setRoot(root);
		

		TreeItem<GState> state = new TreeItem<>();
		for (GState downState : GState.values()) {
			state.getChildren().add(new TreeItem<GState>(downState));
		}
		state.setExpanded(true);
		treeState.setRoot(state);

	}

	void initSelectTreeProperty() {
		treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeView.selectionModelProperty().get().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {

					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {

						
						Type type = treeView.selectionModelProperty().get()
								.getSelectedItem().getValue() ;
					
						if (type == null || type.getName().equals("Downloads")
								|| type.getName().equals("Queues"))
							list.setItems(DownList.AnchorList);
						else {
							ObservableList<Link> obvDown = DownList
									.downType(type);
							list.setItems(DownList.getAnchor(obvDown));
						}

					}
				});
		
		treeState.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeState.selectionModelProperty().get().selectedIndexProperty()
				.addListener((ob, ol, nw) -> {
					DownState st = DownState.values()[nw.intValue()];
					if (st.equals(DownState.InitDown)) {
						list.setItems(DownList.AnchorList);
						return;
					}
					ObservableList<Link> obvDown = DownList.downState(st);
					list.setItems(DownList.getAnchor(obvDown));
				});
	}

	@FXML
	void exportToEF2IDM(ActionEvent event) {
		
		ObservableList<AnchorPane> list = getSelectedItems();
		if(list.isEmpty()) return;
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(R.UserHome));
		chooser.setInitialFileName("links.ef2");
		chooser.setSelectedExtensionFilter(new ExtensionFilter("IDM Url", "ef2"));
		chooser.setTitle("Save Links to IDM formate file");
		File file = chooser.showSaveDialog(null);
		String data = "";
		for (AnchorPane anchorPane : list) {
			int i = DownList.AnchorList.indexOf(anchorPane);
			Link link = DownList.DownloadList.get(i);
			data +=link.getUrl().toIDMString() + "\n";
		}
		
		try {
			FileUtils.writeStringToFile(file, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@FXML
	void exportToJsonFile(ActionEvent event) {

	}

	@FXML
	void exportToTextFile(ActionEvent event) {

	}

	
	
	@FXML
	void importFromEf2IDM(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(R.UserHome));
		chooser.setInitialFileName("links.ef2");
		chooser.setSelectedExtensionFilter(new ExtensionFilter("IDM Url", "ef2"));
		chooser.setTitle("Open Links from IDM formate file");
		File file = chooser.showOpenDialog(null);
		if(file != null) {
			
			new Service<Void>() {

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						
						@Override
						protected Void call() throws Exception {
							List<Url> urls = Url.fromEf2IDMFile(file);
							for (Url url : urls) {
								Item item = new Item(url.url, url.referer);
								item.setState(DownState.InitDown);
								ItemReady ready = new ItemReady(item);
								item = ready.initItem();
								Download download = new Download(item);
								DownList.initGUI(download);
							}
							return null;
						}
					};
				}
			}.start();
			
		}
		
	}

	@FXML
	void importFromJsonFile(ActionEvent event) {

	}

	@FXML
	void importFromTextFile(ActionEvent event) {

	}

	public ObservableList<AnchorPane> getSelectedItems() {
		return list.getSelectionModel().getSelectedItems();
	}

	@FXML
	void addNewDownload(ActionEvent event) {
		FeatchURL.AddURL();
	}

	@FXML
	void startDownload(ActionEvent event) {
		resumeDownloadFile(event);
	}

	@FXML
	void resumeDownloadFile(ActionEvent event) {
		for (AnchorPane anchorPane : getSelectedItems()) {
			int i = DownList.AnchorList.indexOf(anchorPane);
			Link link = DownList.DownloadList.get(i);
			if (link.getState() == State.SCHEDULED) {
				link.start();
			} else if (link.getState() == State.READY) {
				link.start();
			} else if (link.getState() == State.FAILED) {
				link.restart();
			} else if (link.getState() == State.CANCELLED) {
				link.restart();
			} else if (link.getState() == State.SUCCEEDED) {
				if (link.getDownloaded() < link.getLength())
					link.restart();
				// else if(link.getDownState() == DownState.Failed)

			} else if (link.getState() == State.RUNNING) {
				// link.start();
			}

			// if(!link.isRunning()){
			// link.reset();
			// link.start();
			// }
		}
	}

	@FXML
	void pauseAllSelected(ActionEvent event) {
		for (AnchorPane anchorPane : getSelectedItems()) {
			int i = DownList.AnchorList.indexOf(anchorPane);
			Link link = DownList.DownloadList.get(i);
			if (link.isRunning()) {
				link.cancel();
			}

		}
	}

	@FXML
	void pauseAllDownload(ActionEvent event) {
		DownList.pauseAllDownload();
	}

	@FXML
	void deleteSelectedDownload(ActionEvent event) {
		ObservableList<AnchorPane> panes = getSelectedItems();
		for (int i = panes.size(); i >= 0; i--) {
			DownList.removeFromList(panes.get(i));
		}
	}

	@FXML
	void deleteAllComplete(ActionEvent event) {
		for (int i = DownList.DownloadList.size() - 1; i >= 0; i--) {
			Link link = DownList.DownloadList.get(i);
			if (link.getDownState().equals(DownState.Complete)) {
				// if (download.isRunning()) {
				// download.cancel();
				// }
				link.getItem().clearCache();
				DownList.AnchorList.remove(i);
				DownList.DownloadList.remove(i);
			}
		}

	}

	@FXML
	void showQueueManger(ActionEvent event) {

	}

	@FXML
	void startDownloadMainQueue(ActionEvent event) {

	}

	@FXML
	void startDownloadSecondQueue(ActionEvent event) {

	}

	@FXML
	void stopDownloadMainQueue(ActionEvent event) {

	}

	@FXML
	void stopDownloadSecondQueue(ActionEvent event) {

	}
	
	
	@FXML
	void showMinimalView(ActionEvent event){
		MenuItem item = (MenuItem) event.getSource();
		Task<Void> task = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				if(isMinimal){
					for (Item2Gui gui : DownList.guis) {
						gui.fromMin2Item();
					}
					item.setText("Show Minimal View");
				}else{
					for (Item2Gui gui : DownList.guis) {
						gui.fromItem2Min();
					}
						item.setText("Hide Minimal View");
				}
				isMinimal = !isMinimal;
				return null;
			}
		};
		Platform.runLater(task);
	}

	@FXML
	void hideTreeView(ActionEvent event) {

		MenuItem item = (MenuItem) event.getSource();
		if (goLeft) {
			transition.play();
			item.setText("Show Category/Queue");

			hBoxHeader.getChildren().removeAll(deletButton, deletCompButton);
		} else {
			transition.play();
			item.setText("Hide Category/Queue");
			hBoxHeader.getChildren().add(4, deletButton);
			hBoxHeader.getChildren().add(5, deletCompButton);
		}
		
	}

	@FXML
	void showAboutWindows(ActionEvent event) {

	}

	@FXML
	void showOptionsWindows(ActionEvent event) {

	}

	@FXML
	void browseSelectedCategory(ActionEvent event) {

	}

	@FXML
	void showSelectedCategoryInManger(ActionEvent event) {

	}

	@FXML
	void deleteSelectedCategory(ActionEvent event) {

	}

	@FXML
	void addNewCategory(ActionEvent event) {
		AddCategory.addCategory();
	}

	@FXML
	void startDownloadSelectedQueue(ActionEvent event) {

	}

	@FXML
	void stopDownloadSelectedQueue(ActionEvent event) {

	}

	@FXML
	void showSelectedQueueInManger(ActionEvent event) {

	}

	@FXML
	void deleteSelectedQueue(ActionEvent event) {

	}

	@FXML
	void addNewQueue(ActionEvent event) {
		AddQueue.addQueue();
	}

	@FXML
	void closePrograme(ActionEvent event) {
		stage.close();
	}

}
