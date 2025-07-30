package be.esmay.pocketidsync.api.models;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class PagedResponse<T> {

    private List<T> data;
    private int page;
    private int limit;
    private int totalPages;
    private long totalItems;

}