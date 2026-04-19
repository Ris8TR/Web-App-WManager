import os
from pathlib import Path

def generate_tree(dir_path, prefix=""):
    """
    Genera ricorsivamente la struttura ad albero di una cartella.
    """
    # Lista i file e le cartelle (escludendo file nascosti o script stesso)
    entries = sorted(list(dir_path.iterdir()), key=lambda x: (x.is_file(), x.name.lower()))
    
    # Filtra lo script stesso e il file di output per non includerli nel log
    entries = [e for e in entries if e.name not in ["tree_generator.py", "struttura_progetto.txt"]]
    
    tree_str = ""
    count = len(entries)
    
    for i, entry in enumerate(entries):
        is_last = (i == count - 1)
        connector = "└── " if is_last else "├── "
        
        # Aggiunge l'elemento corrente
        tree_str += f"{prefix}{connector}{entry.name}\n"
        
        # Se è una cartella, scende ricorsivamente
        if entry.is_dir():
            new_prefix = prefix + ("    " if is_last else "│   ")
            tree_str += generate_tree(entry, new_prefix)
            
    return tree_str

def main():
    # Ottiene la cartella corrente dove si trova lo script
    current_path = Path.cwd()
    output_file = "struttura_progetto.txt"
    
    print(f"Generazione gerarchia per: {current_path}...")
    
    header = f"{current_path.name}/\n"
    tree_content = generate_tree(current_path)
    
    full_output = header + tree_content
    
    # Scrive l'output su file con codifica UTF-8 per supportare i simboli grafici
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(full_output)
        
    print(f"Completato! La struttura è stata salvata in: {output_file}")

if __name__ == "__main__":
    main()