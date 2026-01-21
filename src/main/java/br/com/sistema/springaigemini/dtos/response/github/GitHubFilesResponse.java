package br.com.sistema.springaigemini.dtos.response.github;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response com estrutura de arquivos de um repositório.
 * 
 * Usado pelo endpoint /api/v1/github-selector/repos/{name}/files
 * Retorna árvore de arquivos/pastas pronta para frontend renderizar com checkboxes.
 * 
 * ✅ CORRIGIDO: Convertido de record para classe mutável (com Lombok)
 * Agora é possível modificar children quando navegando recursivamente!
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubFilesResponse {
    
    private String repositoryName;
    private List<FileNode> files;
    private Integer totalFiles;

    /**
     * Representa um arquivo ou pasta na árvore de arquivos
     * 
     * ✅ AGORA MUTÁVEL: Pode-se fazer node.children.addAll(...)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileNode {
        private String name;           // nome do arquivo/pasta
        private String path;           // caminho relativo (ex: "src/main")
        private String type;           // "file" ou "directory"
        private List<FileNode> children;  // null se for file, [] se directory vazio
        private String extension;      // ex: ".java", ".json", null se directory
        private Long size;             // tamanho em bytes, null se directory

        /**
         * Helper: Cria um FileNode de arquivo
         */
        public static FileNode ofFile(String name, String path, String extension) {
            return FileNode.builder()
                .name(name)
                .path(path)
                .type("file")
                .extension(extension)
                .children(null)
                .size(null)
                .build();
        }

        /**
         * Helper: Cria um FileNode de diretório
         */
        public static FileNode ofDirectory(String name, String path) {
            return FileNode.builder()
                .name(name)
                .path(path)
                .type("directory")
                .extension(null)
                .children(new ArrayList<>())  // ✅ Inicia com lista vazia
                .size(null)
                .build();
        }

        /**
         * Adiciona um filho ao diretório
         * ✅ Agora funciona porque é mutável!
         */
        public void addChild(FileNode child) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.add(child);
        }

        /**
         * Adiciona múltiplos filhos
         */
        public void addChildren(List<FileNode> childrenList) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.addAll(childrenList);
        }

        /**
         * Verifica se é um diretório
         */
        public boolean isDirectory() {
            return "directory".equals(this.type);
        }

        /**
         * Verifica se é um arquivo
         */
        public boolean isFile() {
            return "file".equals(this.type);
        }
    }
}