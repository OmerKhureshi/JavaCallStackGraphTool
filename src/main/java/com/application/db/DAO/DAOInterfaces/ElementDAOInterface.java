package com.application.db.DAO.DAOInterfaces;

import com.application.fxgraph.ElementHelpers.Element;

public interface ElementDAOInterface extends  TableDAOInterface{
    void insert(Element element);
}
