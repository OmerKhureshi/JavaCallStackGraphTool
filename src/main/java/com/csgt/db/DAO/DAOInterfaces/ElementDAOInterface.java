package com.csgt.db.DAO.DAOInterfaces;

import com.csgt.controller.ElementHelpers.Element;

public interface ElementDAOInterface extends  TableDAOInterface{
    void insert(Element element);
}
