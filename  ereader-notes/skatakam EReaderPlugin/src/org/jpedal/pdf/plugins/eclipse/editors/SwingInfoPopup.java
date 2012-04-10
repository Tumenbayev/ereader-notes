/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
 @LICENSE@
  *
  * ---------------

  * InfoFactory.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.pdf.plugins.eclipse.editors;

import org.jpedal.PdfDecoder;
import org.jpedal.pdf.plugins.eclipse.settings.PDFSettings;
import org.jpedal.utils.BrowserLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class SwingInfoPopup {
    public void popupInfoBox(Component parent) {

        /**popup window display message about plugin*/
        try {
            final JPanel info = new JPanel();
            info.setLayout(new BorderLayout());
            JLabel lab = new JLabel(new ImageIcon(getClass().getResource("/icons/ceo.jpg")));

            info.add(lab, BorderLayout.NORTH);

            final JLabel message = new JLabel(
                    "<html>http://www.jpedal.org");
            message.setHorizontalAlignment(JLabel.CENTER);
            message.setForeground(Color.blue);
            message.setFont(new Font("Lucida", Font.PLAIN, 16));

            message.addMouseListener(new MouseListener() {
                public void mouseEntered(MouseEvent e) {
                    info.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    message.setText("<html><info href=http://www.jpedal.org>http://www.jpedal.org</info>");
                }

                public void mouseExited(MouseEvent e) {
                    info.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    message.setText("<html>http://www.jpedal.org");
                }

                public void mouseClicked(MouseEvent e) {
                    try {
                        BrowserLauncher.openURL("http://www.jpedal.org");
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Unable to launch browser");
                    }
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }
            });

            info.add(message, BorderLayout.CENTER);

            final JLabel version = new JLabel(
                    "<html>"+ PDFSettings.version);
            
            version.setHorizontalAlignment(JLabel.CENTER);
            version.setForeground(Color.black);
            version.setFont(new Font("Lucida", Font.PLAIN, 12));

            info.add(version, BorderLayout.SOUTH);

            info.setPreferredSize(new Dimension(300, 214));
            Object[] options = {"Close"};
            JOptionPane.showOptionDialog(parent, info, "PDF Viewer for Eclipse",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
