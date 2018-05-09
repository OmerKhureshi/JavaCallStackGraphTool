package com.csgt.db.DAO.DAOInterfaces;

public interface ElementToChildDAOInterface extends TableDAOInterface{
    void insert(int elementId, int childId);
}

