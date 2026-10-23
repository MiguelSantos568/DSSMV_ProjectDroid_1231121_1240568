package com.example.condosched.domain.DTO;

public class BuildingDTO {
    private long id;
    private String name;
    private long condominiumId;

    public BuildingDTO(long id, String name, long condominiumId) {
        this.id = id;
        this.name = name;
        this.condominiumId = condominiumId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCondominiumId() {
        return condominiumId;
    }

    public void setCondominiumId(long condominiumId) {
        this.condominiumId = condominiumId;
    }
}
