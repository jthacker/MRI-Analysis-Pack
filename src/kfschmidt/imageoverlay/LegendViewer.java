package kfschmidt.imageoverlay;

/**
 *
 *
 *
 *   @author Karl Schmidt <karl.schmidt@umassmed.edu>
 *   This software is provided for use free of any costs,
 *   Be advised that NO guarantee is made regarding it's quality,
 *   and there is no ongoing support for this codebase.
 *
 *   (c) Karl Schmidt 2003
 *
 *   REVISION HISTORY:
 *
 *
 */
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;

public class LegendViewer extends JFrame {
	Legend mLegend;
	LegendPanel mLPanel;

	public LegendViewer(Legend legend) {
		mLegend = legend;
		getContentPane().setLayout(new GridLayout(1, 1));
		mLPanel = new LegendPanel(mLegend.getLegend());
		getContentPane().add(mLPanel);
		setTitle(Literals.LEGEND_TITLE);
		checkSize();
		refresh();
	}

	public void refresh() {
		mLegend.refreshLegend();
		mLPanel.setLegend(mLegend.getLegend());
		repaint();
	}

	private void checkSize() {
		int margin = 20;
		setSize(mLPanel.getLegend().getWidth() + margin, mLPanel.getLegend().getHeight() + margin + 20);
		validate();
	}

	public BufferedImage getRGBLegend() {
		BufferedImage bi = new BufferedImage(mLPanel.getWidth(), mLPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
		mLPanel.paint(bi.getGraphics());
		return bi;
	}

}

class LegendPanel extends JPanel {
	BufferedImage mLegend;
	int margin = 5;

	LegendPanel(BufferedImage legend) {
		setLegend(legend);
	}

	public void setLegend(BufferedImage bi) {
		mLegend = bi;
		setSize(bi.getWidth() + 2 * margin, bi.getHeight() + 2 * margin);
	}

	BufferedImage getLegend() {
		return mLegend;
	}

	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(mLegend, margin, margin, mLegend.getWidth(), mLegend.getHeight(), null);
	}

}
