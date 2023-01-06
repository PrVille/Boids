import javafx.stage.Screen
import javafx.util.Duration
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control.{Button, CheckBox, Label, Slider, Tooltip}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

object Main extends JFXApp {

  var maximized = false
  val root = new Scene
  val controlWidth = 150 //width of sidebox with live controls
  var canvasOption: Option[Canvas] =
    None //For determening what size to launch the program in

  //stage
  stage = new JFXApp.PrimaryStage {
    title.value = "Flock simulation"
    width = 1366
    height = 768
    scene = root
  }

  //changing variable maximized to true will launch the app maximized to the screen
  stage.setMaximized(maximized)
  stage.setResizable(false) //Disables resizing the window

  //what size to launch in
  if (maximized) {
    canvasOption = Some(
      new Canvas(
        Screen.getPrimary.getBounds.getWidth - controlWidth,
        Screen.getPrimary.getBounds.getHeight - 40
      )
    )
  } else {
    canvasOption = Some(
      new Canvas(stage.getWidth - controlWidth, stage.getHeight - 40)
    ) //if not full screen
  }

  var canvas: Canvas = canvasOption.get
  canvas.setTranslateX(
    controlWidth
  ) //Move canvas to the right to adjust the border between controls and canvas
  //Getting the GraphicsContext
  val g = canvas.graphicsContext2D
  root.getChildren += canvas //Add canvas to GUI.

  //the simulation
  val simulation = new Simulation(canvas.width.toDouble, canvas.height.toDouble)

  /** Sliders
    */

  val separationSlider = new Slider(0, 1, 0.25)
  separationSlider.setShowTickMarks(true)
  separationSlider.setShowTickLabels(true)
  separationSlider.setMajorTickUnit(0.25f)
  separationSlider.setBlockIncrement(0.1f)
  separationSlider.setPrefWidth(controlWidth)
  separationSlider.setTranslateY(30)
  root.getChildren.add(separationSlider)

  val alignmentSlider = new Slider(0, 1, 0.75)
  alignmentSlider.setShowTickMarks(true)
  alignmentSlider.setShowTickLabels(true)
  alignmentSlider.setMajorTickUnit(0.25f)
  alignmentSlider.setBlockIncrement(0.1f)
  alignmentSlider.setPrefWidth(controlWidth)
  alignmentSlider.setTranslateY(100)
  root.getChildren.add(alignmentSlider)

  val cohesionSlider = new Slider(0, 1, 0.75)
  cohesionSlider.setShowTickMarks(true)
  cohesionSlider.setShowTickLabels(true)
  cohesionSlider.setMajorTickUnit(0.25f)
  cohesionSlider.setBlockIncrement(0.1f)
  cohesionSlider.setPrefWidth(controlWidth)
  cohesionSlider.setTranslateY(170)
  root.getChildren.add(cohesionSlider)

  val radiusSlider = new Slider(0, 100, 75)
  radiusSlider.setShowTickMarks(true)
  radiusSlider.setShowTickLabels(true)
  radiusSlider.setMajorTickUnit(25)
  radiusSlider.setBlockIncrement(10)
  radiusSlider.setPrefWidth(controlWidth)
  radiusSlider.setTranslateY(240)
  root.getChildren.add(radiusSlider)

  /** Labels
    */

  val cohesionSliderLabel = new Label("Cohesion")
  val cohesionWidth =
    new Text(
      "Cohesion"
    ).getLayoutBounds.getWidth //to get the width of the text, couldn't do it from label
  cohesionSliderLabel.setTranslateX((controlWidth - cohesionWidth) / 2)
  cohesionSliderLabel.setTranslateY(145)
  root.getChildren.add(cohesionSliderLabel)

  val alignmentSliderLabel = new Label("Alignment")
  val alignmentWidth =
    new Text(
      "Alignment"
    ).getLayoutBounds.getWidth //to get the width of the text, couldn't do it from label
  alignmentSliderLabel.setTranslateX((controlWidth - alignmentWidth) / 2)
  alignmentSliderLabel.setTranslateY(75)
  root.getChildren.add(alignmentSliderLabel)

  val separationSliderLabel = new Label("Separation")
  val separationWidth =
    new Text(
      "Separation"
    ).getLayoutBounds.getWidth //to get the width of the text, couldn't do it from label
  separationSliderLabel.setTranslateX((controlWidth - separationWidth) / 2)
  separationSliderLabel.setTranslateY(5)
  root.getChildren.add(separationSliderLabel)

  val radiusSliderLabel = new Label("Visual range")
  val radiusWidth =
    new Text(
      "Visual range"
    ).getLayoutBounds.getWidth //to get the width of the text, couldn't do it from label
  radiusSliderLabel.setTranslateX((controlWidth - radiusWidth) / 2)
  radiusSliderLabel.setTranslateY(215)
  root.getChildren.add(radiusSliderLabel)

  val boidAmountLabel = new Label()
  boidAmountLabel.setTranslateY(canvas.getHeight - 45)
  boidAmountLabel.setText(s"Boids: ${simulation.getBoidAmount}")
  root.getChildren.add(boidAmountLabel)

  val boidTotalSpeedLabel = new Label()
  boidTotalSpeedLabel.setTranslateY(canvas.getHeight - 30)
  boidTotalSpeedLabel.setText(
    "Boid Max Speed: " + f"${simulation.getBoidSpeed}%1.1f"
  )
  root.getChildren.add(boidTotalSpeedLabel)

  val boidSpeedLabel = new Label("Speed")
  boidSpeedLabel.setTranslateY(325)
  root.getChildren.add(boidSpeedLabel)

  val boidTotalSizeLabel = new Label()
  boidTotalSizeLabel.setTranslateY(canvas.getHeight - 15)
  boidTotalSizeLabel.setText("Boid Size: " + f"${simulation.getBoidSize}%1.1f")
  root.getChildren.add(boidTotalSizeLabel)

  val boidSizeLabel = new Label("Size")
  boidSizeLabel.setTranslateY(350)
  root.getChildren.add(boidSizeLabel)

  val boidAmntLabel = new Label("Boids")
  boidAmntLabel.setTranslateY(300)
  root.getChildren.add(boidAmntLabel)

  val deadBoidsCountLabel = new Label()
  deadBoidsCountLabel.setTranslateY(canvas.getHeight - 60)
  deadBoidsCountLabel.setText(s"Dead Boids: ${simulation.getDeadBoidAmount}")
  root.getChildren.add(deadBoidsCountLabel)

  val supriseLabel = new Label()
  supriseLabel.setTranslateY(550)
  supriseLabel.setText(
    "You have now watched 10000 boids die and did nothing about it, you monster!"
  )
  supriseLabel.setPrefWidth(controlWidth)
  supriseLabel.wrapText = true
  supriseLabel.setFont(Font.font("Verdana", FontWeight.Bold, 15))
  supriseLabel.setTextFill(Color.Red)
  root.getChildren.add(supriseLabel)
  supriseLabel.visible = false

  //info
  val infoLabel = new Label("?")
  infoLabel.setTranslateX(5)
  infoLabel.setFont(Font.font("Times new Roman", FontWeight.Normal, 15))
  val infoTooltip = new Tooltip(
    "Boid flocks are simulated by giving each boid simple rules to choose its direction of travel. " +
      "When there are several boids and they work according to the same internal rules, their co-operation resembles that of real birds. " +
      "\nControl limits:" +
      "\nBoids min amount 0, max amount 500" +
      "\nBoids min speed 2.0, max speed 6.0" +
      "\nBoids min size 0.5, max size 2.0"
  )
  infoTooltip.setPrefWidth(controlWidth * 2)
  infoTooltip.wrapText = true
  infoTooltip.setShowDelay(
    Duration.millis(1)
  ) //to show tooltip faster than default
  infoLabel.setTooltip(infoTooltip)
  root.getChildren.add(infoLabel)

  /** Buttons
    */

  val addBoidButton = new Button("+")
  addBoidButton.setTranslateY(300)
  addBoidButton.setTranslateX(80)
  addBoidButton.setPrefWidth(25)
  addBoidButton.onAction = (event: ActionEvent) => {
    simulation.addRandoms(25)
    boidAmountLabel.setText(s"Boids: ${simulation.getBoidAmount}")
  }
  root.getChildren.add(addBoidButton)

  val removeBoidButton = new Button("-")
  removeBoidButton.setTranslateY(300)
  removeBoidButton.setTranslateX(50)
  removeBoidButton.setPrefWidth(25)
  removeBoidButton.onAction = (event: ActionEvent) => {
    simulation.removeRandoms(25)
    boidAmountLabel.setText(s"Boids: ${simulation.getBoidAmount}")
  }
  root.getChildren.add(removeBoidButton)

  val decreaseSpeedButton = new Button("-")
  decreaseSpeedButton.setTranslateY(325)
  decreaseSpeedButton.setTranslateX(50)
  decreaseSpeedButton.setPrefWidth(25)
  decreaseSpeedButton.onAction = (event: ActionEvent) => {
    simulation.decreaseSpeed()
    boidTotalSpeedLabel.setText(
      "Boid Max Speed: " + f"${simulation.getBoidSpeed}%1.1f"
    )
  }
  root.getChildren.add(decreaseSpeedButton)

  val increaseSpeedButton = new Button("+")
  increaseSpeedButton.setTranslateY(325)
  increaseSpeedButton.setTranslateX(80)
  increaseSpeedButton.setPrefWidth(25)
  increaseSpeedButton.onAction = (event: ActionEvent) => {
    simulation.increaseSpeed()
    boidTotalSpeedLabel.setText(
      "Boid Max Speed: " + f"${simulation.getBoidSpeed}%1.1f"
    )
  }
  root.getChildren.add(increaseSpeedButton)

  val increaseSize = new Button("+")
  increaseSize.setTranslateY(350)
  increaseSize.setTranslateX(80)
  increaseSize.setPrefWidth(25)
  increaseSize.onAction = (event: ActionEvent) => {
    simulation.increaseSize()
    boidTotalSizeLabel.setText(
      "Boid Size: " + f"${simulation.getBoidSize}%1.1f"
    )
  }
  root.getChildren.add(increaseSize)

  val decreaseSize = new Button("-")
  decreaseSize.setTranslateY(350)
  decreaseSize.setTranslateX(50)
  decreaseSize.setPrefWidth(25)
  decreaseSize.onAction = (event: ActionEvent) => {
    simulation.decreaseSize()
    boidTotalSizeLabel.setText(
      "Boid Size: " + f"${simulation.getBoidSize}%1.1f"
    )
  }
  root.getChildren.add(decreaseSize)

  val resetFlockButton = new Button("Reset Flock")
  resetFlockButton.setTranslateY(canvas.getHeight - 85)
  resetFlockButton.onAction = (event: ActionEvent) => {
    simulation.resetFlock()
  }
  root.getChildren.add(resetFlockButton)

  /** Checkboxes
    */

  var predatorCheck = false
  val addPredatorBox = new CheckBox("Predator")
  addPredatorBox.setTranslateY(400)
  addPredatorBox.onAction = (event: ActionEvent) => {
    if (!predatorCheck) {
      simulation.addPredator()
      predatorCheck = true
    } else {
      simulation.removePredator()
      predatorCheck = false
    }
  }
  root.getChildren.add(addPredatorBox)

  var obstacleCheck = false
  val toggleObstacleBox = new CheckBox("Obstacle")
  toggleObstacleBox.setTranslateY(420)
  toggleObstacleBox.onAction = (event: ActionEvent) => {
    if (!obstacleCheck) {
      simulation.addObstacle()
      obstacleCheck = true
    } else {
      simulation.removeObstacle()
      obstacleCheck = false
    }
  }
  root.getChildren.add(toggleObstacleBox)

  //if mortality is not checked dead boids count label and predator turbo mode is not visible
  var deadBoidsCheck = false
  val toggleDeadBoidsBox = new CheckBox("Mortality")
  toggleDeadBoidsBox.setTranslateY(460)
  toggleDeadBoidsBox.onAction = (event: ActionEvent) => {
    simulation.toggleDeadBoids()
    if (!deadBoidsCheck)
      deadBoidsCheck = true
    else {
      toggleTurboModeBox.selected = false
      simulation.toggleTurboMode()
      deadBoidsCheck = false
    }
  }
  root.getChildren.add(toggleDeadBoidsBox)

  toggleDeadBoidsBox.selected <==> deadBoidsCountLabel.visible
  toggleDeadBoidsBox.selected = false

  val toggleRacismButton = new CheckBox("Racism")
  toggleRacismButton.setTranslateY(440)
  toggleRacismButton.onAction = (event: ActionEvent) => {
    simulation.toggleRacism()
  }
  root.getChildren.add(toggleRacismButton)

  val toggleTurboModeBox = new CheckBox("Predator Turbomode")
  toggleTurboModeBox.setTranslateY(500)
  toggleTurboModeBox.onAction = (event: ActionEvent) => {
    simulation.toggleTurboMode()
  }
  root.getChildren.add(toggleTurboModeBox)

  toggleDeadBoidsBox.selected <==> toggleTurboModeBox.visible
  toggleDeadBoidsBox.selected = false

  val autoSpawnBox = new CheckBox("Autospawn boids")
  autoSpawnBox.setTranslateY(480)
  autoSpawnBox.onAction = (event: ActionEvent) => {
    simulation.toggleAutoSpawn()
  }
  root.getChildren.add(autoSpawnBox)

  def animate() = {
    g.clearRect(0, 0, canvas.getWidth, canvas.getHeight)
    simulation.draw(g)
    simulation.update(
      separationSlider.getValue,
      alignmentSlider.getValue,
      cohesionSlider.getValue,
      radiusSlider.getValue
    )
    boidAmountLabel.setText(s"Boids: ${simulation.getBoidAmount}")
    deadBoidsCountLabel.setText(s"Dead Boids: ${simulation.getDeadBoidAmount}")
    if (simulation.getDeadBoidAmount >= 10000) supriseLabel.visible = true
  }

  val ticker = new Ticker(animate)
  ticker.start()

}
