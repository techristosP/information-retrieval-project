package utils;

public record MyFile(String id, String text) {

    @Override
    public String toString() {
        return "File {" + "id=" + id + ", text=" + text + '}';
    }

}
