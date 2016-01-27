/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;

/**
 *
 * @author Taihao
 */
public interface HighlightingRoiCollectionContainer {
    public void addCollection(HighlightingRoiCollectionNode aCollection);
    public HighlightingRoiCollectionNode getCollection(String sID);
    public void removeCollection(String sID);
    public void showHighlightCollections();
    public void setHighlight(boolean highlight);
}
