package de.mediathekview.mserver.ui.gui;

import java.util.Locale;
import java.util.ResourceBundle;

import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MServerGUI extends Application {

	private final CrawlerManager crawlerManager;

	public MServerGUI() {
		crawlerManager = CrawlerManager.getInstance();
	}

	@Override
	public void start(final Stage aPrimaryStage) throws Exception {
		ResourceBundle bundle = ResourceBundle.getBundle("MServerGUI", Locale.getDefault());
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/MServerGUI.fxml"),bundle);
	    
        Scene scene = new Scene(root);
    
        aPrimaryStage.setScene(scene);
        aPrimaryStage.show();
	}
	
	@FXML
	protected void removeAll()
	{
		
	}
	
	@FXML
	protected void removeSelected()
	{
		
	}
	
	@FXML
	protected void addSelected()
	{
		
	}
	@FXML
	protected void addAll()
	{
		
	}

}
