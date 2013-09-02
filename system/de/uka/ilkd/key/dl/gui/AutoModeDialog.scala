package de.uka.ilkd.key.dl.gui

import scala.swing._
import javax.swing.Icon
import javax.swing.text.DefaultCaret
import java.beans.{PropertyChangeEvent, PropertyChangeListener}

/**
 * Dialog that is shown while the automode is running
 * User: jdq
 * Date: 8/30/13
 * Time: 10:20 PM
 */
object AutoModeDialog extends Dialog {
  title = "Automatic Strategy"
  val stopButton = new Button {
  }
  val systemOut = new TextArea() {
    editable = false
    val myCaret = peer.getCaret.asInstanceOf[DefaultCaret]
    myCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
  }

  def appendToSystemOut(text: String) = {
    systemOut.append(text)
  }

  val systemErr = new TextArea() {
    editable = false
    val myCaret = peer.getCaret.asInstanceOf[DefaultCaret]
    myCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
  }

  def appendToSystemErr(text: String) = {
    systemErr.append(text)
  }

  val panel = new BoxPanel(Orientation.Vertical) {
    contents += new Label("Automatic Strategy is running...")
    private val sysOutScrollPane = new ScrollPane() {
      border = Swing.LineBorder(java.awt.Color.BLACK)
      preferredSize = new Dimension(700, 200);
      contents = systemOut
      peer.setAutoscrolls(true)
    }
    sysOutScrollPane.peer.setAutoscrolls(true)
    contents += sysOutScrollPane
    private val sysErrScrollPane = new ScrollPane() {
      border = Swing.LineBorder(java.awt.Color.BLACK)
      preferredSize = new Dimension(700, 200);
      contents = systemErr
      peer.setAutoscrolls(true)
    }
    sysErrScrollPane.peer.setAutoscrolls(true)
    contents += sysErrScrollPane
    contents += stopButton
  }
  contents = panel
  centerOnScreen


  def setAction(act: javax.swing.Action) = {
    stopButton.action = new Action(act.getValue(javax.swing.Action.NAME).asInstanceOf[String]) {
      icon = act.getValue(javax.swing.Action.SMALL_ICON).asInstanceOf[Icon]
      enabled = act.isEnabled
      def apply = act.actionPerformed(null)
      act.addPropertyChangeListener(new PropertyChangeListener {
        def propertyChange(evt: PropertyChangeEvent) {
          enabled = act.isEnabled
          icon = act.getValue(javax.swing.Action.SMALL_ICON).asInstanceOf[Icon]
          title = act.getValue(javax.swing.Action.NAME).asInstanceOf[String]
        }
      })
    }
  }
}
