package br.com.sistema.springaigemini.dtos.response.github;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representa um nó na árvore de arquivos do GitHub
 * Pode ser um arquivo ou uma pasta com sub-itens
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileNode {
    
    private String name;              // Nome do arquivo/pasta
    private String path;              // Caminho completo (ex: "src/main/java")
    private String type;              // "file" ou "folder"
    private String extension;         // Extensão do arquivo (ex: "java", "xml")
    private Long size;                // Tamanho em bytes (null para pastas)
    private List<FileNode> children;  // Sub-itens (null para arquivos)
    
    // ==================== CONSTRUTORES ====================
    
    public FileNode() {}
    
    public FileNode(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }
    
    public FileNode(String name, String path, String type, String extension) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.extension = extension;
    }
    
    // ==================== GETTERS E SETTERS ====================
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public List<FileNode> getChildren() {
        return children;
    }
    
    public void setChildren(List<FileNode> children) {
        this.children = children;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Adiciona um filho a este nó (deve ser uma pasta)
     */
    public void addChild(FileNode child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
    
    /**
     * Verifica se é uma pasta
     */
    public boolean isFolder() {
        return "folder".equals(this.type);
    }
    
    /**
     * Verifica se é um arquivo
     */
    public boolean isFile() {
        return "file".equals(this.type);
    }
    
    /**
     * Extrai a extensão do nome do arquivo
     * Exemplo: "Main.java" → "java"
     */
    public static String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Conta recursivamente o total de arquivos (não pastas)
     */
    public int countFiles() {
        if (this.isFile()) {
            return 1;
        }
        
        int count = 0;
        if (this.children != null) {
            for (FileNode child : this.children) {
                count += child.countFiles();
            }
        }
        return count;
    }
    
    @Override
    public String toString() {
        return "FileNode{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", children=" + (children != null ? children.size() : 0) +
                '}';
    }
}