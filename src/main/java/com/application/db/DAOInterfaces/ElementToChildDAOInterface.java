package com.application.db.DAOInterfaces;

public interface ElementToChildDAOInterface extends TableDAOInterface{
    void insert(int elementId, int childId);
}

