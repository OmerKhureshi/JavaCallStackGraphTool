package com.csgt.controller.modules;

import com.csgt.db.DAO.DAOImplementation.*;
import com.csgt.controller.ElementHelpers.EdgeElement;
import com.csgt.controller.ElementHelpers.Element;

import java.util.*;

public class ElementTreeModule {
    public static Element greatGrandParent;
    private Map<Integer, Element> threadMapToRoot;
    public ArrayList<Element> rootsList;
    Element grandParent, parent, cur;
    Map<Integer, Element> currentMap;
    private String currentThreadId = "0";

    private boolean showAllThreads = true;

    public int multiplierForVisibleViewPort = 3;
    public int multiplierForPreLoadedViewPort = 2;

    public ElementTreeModule() {
        Element.clearAutoIncrementId();
        greatGrandParent = new Element(null, -2);
        rootsList = new ArrayList<>();
        currentMap = new HashMap<>();
        threadMapToRoot = new LinkedHashMap<>();
    }

    /**
     * This method converts the string to an element tree.
     */
    public void StringToElementList(List<String> line, int fkCallTrace) {
        String msg = line.get(3);
        Integer threadId = Integer.valueOf(line.get(2));

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
                IllegalStateException up = new IllegalStateException("EventType should be either ENTER OR EXIT. This line caused exception: " + line);
                throw up;  // Yuck! Not having any of that :(
        }

        if (parent == null &&
                (!msg.equalsIgnoreCase("EXIT") &&
                        !msg.equalsIgnoreCase("WAIT-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFY-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFYALL-EXIT"))) {
            if (!threadMapToRoot.containsKey(threadId)) {
                grandParent = new Element(greatGrandParent, -1);
                grandParent.setChildren(new ArrayList<>(Arrays.asList(cur)));
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

        /*if ( msg.equalsIgnoreCase("ENTER")) {
            defaultInitialize(cur);
            ElementDAOImpl.insert(cur);
        }*/
    }

    private void defaultInitialize(Element element) {
        cur.setLeafCount(-1);
        cur.setLevelCount(-1);
        cur.getBoundBox().xTopLeft = -1;
        cur.getBoundBox().yTopLeft = -1;
        cur.getBoundBox().xTopRight = -1;
        cur.getBoundBox().yTopRight = -1;
        cur.getBoundBox().xBottomRight = -1;
        cur.getBoundBox().yBottomRight = -1;
        cur.getBoundBox().xBottomLeft = -1;
        cur.getBoundBox().yBottomLeft = -1;
    }

    public Map<Integer, Element> getThreadMapToRoot() {
        return threadMapToRoot;
    }


    /**
     * Calculates the Element properties on all direct and indirect children of current element.
     * Ensure that the sub tree is fully constructed before invoking this method.
     */
    public void calculateElementProperties() {
        greatGrandParent.calculateLeafCount();
        greatGrandParent.calculateLevelCount(0);

        greatGrandParent.getChildren().stream().forEach(element -> {
            element.setBoundBoxOnAll(element);
        });
        // greatGrandParent.setBoundBoxOnAll(greatGrandParent);

    }

    public void recursivelyInsertElementsIntoDB(Element root) {
        if (root == null)
            return;
        ElementDAOImpl.insert(root);
        ElementToChildDAOImpl.insert(
                root.getParent() == null ? -1 : root.getParent().getElementId(),
                root.getElementId());
        // // Create and insert Edges.
        // Edge edge = new Edge(root.getParent(), root);
        // edge.setStartX();

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(this::recursivelyInsertElementsIntoDB);
    }

    public void recursivelyInsertEdgeElementsIntoDB(Element root) {
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
                // EdgeDAOImpl.insert(edgeElement);

                edgeElementList.add(edgeElement);

                recursivelyInsertEdgeElementsIntoDB(targetElement);
            });
    }
}

