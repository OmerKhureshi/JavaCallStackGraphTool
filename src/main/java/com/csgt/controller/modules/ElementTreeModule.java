package com.csgt.controller.modules;

import com.csgt.dataaccess.model.EdgeElement;
import com.csgt.dataaccess.model.Element;
import com.csgt.dataaccess.DAO.EdgeDAOImpl;

import java.util.*;

/**
 * This class handles all the activities associated with the tree during the application startup.
 */
public class ElementTreeModule {
    public static Element greatGrandParent;
    private Map<Integer, Element> threadMapToRoot;

    private Element parent;
    private Map<Integer, Element> currentMap;

    ElementTreeModule() {
        Element.clearAutoIncrementId();
        greatGrandParent = new Element(null, -2);
        currentMap = new HashMap<>();
        threadMapToRoot = new LinkedHashMap<>();
    }

    /**
     * This method converts the string to an element tree.
     */
    public void StringToElementList(List<String> line, int fkCallTrace) {
        String msg = line.get(3);
        Integer threadId = Integer.valueOf(line.get(2));

        Element cur;
        switch (msg.toUpperCase()) {
            case "WAIT-ENTER":
            case "NOTIFY-ENTER":
            case "NOTIFYALL-ENTER":
            case "ENTER":
                if (!threadMapToRoot.containsKey(threadId)) {
                    // new thread
                    parent = null;
                } else if (currentMap.containsKey(threadId)) {
                    parent = currentMap.get(threadId);
                    // parent = cur;
                }
                cur = new Element(parent, fkCallTrace);
                currentMap.put(threadId, cur);
                break;

            case "WAIT-EXIT":
            case "NOTIFY-EXIT":
            case "NOTIFYALL-EXIT":
            case "EXIT":
                cur = currentMap.get(threadId);
                cur.setFkExitCallTrace(fkCallTrace);
                cur = cur.getParent();
                currentMap.put(threadId, cur);
                // cur = cur.getParent();
                break;

            default:
                throw new IllegalStateException("EventType should be either ENTER OR EXIT. This line caused exception: " + line);  // Yuck! Not having any of that :(
        }

        if (parent == null &&
                (!msg.equalsIgnoreCase("EXIT") &&
                        !msg.equalsIgnoreCase("WAIT-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFY-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFYALL-EXIT"))) {
            if (!threadMapToRoot.containsKey(threadId)) {
                Element grandParent = new Element(greatGrandParent, -1);
                grandParent.setChildren(new ArrayList<>(Collections.singletonList(cur)));
                cur.setParent(grandParent);
                threadMapToRoot.put(threadId, grandParent);
                /*defaultInitialize(grandParent);
                ElementDAOImpl.insert(grandParent);*/
            } else {
                Element grandparent = threadMapToRoot.get(threadId);   // Get grandParent root for the current threadId
                grandparent.setChildren(new ArrayList<>(Collections.singletonList(cur)));       // set the current element as the child of the grandParent element.
                cur.setParent(grandparent);
            }
        }
    }


    /**
     * Calculates the Element properties on all direct and indirect children of current element.
     * Ensure that the sub tree is fully constructed before invoking this method.
     */
    public void calculateElementProperties() {
        greatGrandParent.calculateLeafCount();
        greatGrandParent.calculateLevelCount(0);

        greatGrandParent.getChildren().forEach(element -> element.setBoundBoxOnAll(element));
    }

    private void recursivelyInsertEdgeElementsIntoDB(Element root) {
        if (root == null)
            return;

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(targetElement -> {
                EdgeElement edgeElement = new EdgeElement(root, targetElement);
                edgeElement.calculateEndPoints();
                EdgeDAOImpl.insert(edgeElement);

                recursivelyInsertEdgeElementsIntoDB(targetElement);
            });
    }

    public void recursivelyInsertEdgeElementsIntoDB(Element root, List<EdgeElement> edgeElementList) {
        if (root == null)
            return;

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(targetElement -> {
                EdgeElement edgeElement = new EdgeElement(root, targetElement);
                edgeElement.calculateEndPoints();
                edgeElementList.add(edgeElement);

                recursivelyInsertEdgeElementsIntoDB(targetElement);
            });
    }
}

