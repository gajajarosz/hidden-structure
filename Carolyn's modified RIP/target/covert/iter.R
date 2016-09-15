args <- commandArgs(TRUE)
its <- as.numeric(args[1]) #how many files are there
name <- args[2] #file name minus index
or_list <- list() #list of learning paths (syllable orders)
for (f in seq(1,its)) #iterate through files
 {
    file = paste(paste(f,name,sep="-"),"csv",sep=".") #create file name
    data <- read.csv(file, header=TRUE) #read in file
    data <- data[data[["syllable"]] == "[CV]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[V]" | data[["syllable"]] =="[VC]" | data[["syllable"]] =="[CVC]" | data[["syllable"]] =="[CCV]" | data[["syllable"]] =="[VCC]" | data[["syllable"]] =="[CCVC]" | data[["syllable"]] =="[CVCC]" | data[["syllable"]] =="[CCVCC]", ]
    syll_acc <- list() #List of earliest accurate production of each syllable (list of (iteration,syllable) pairs)
    for (syll in data[1:9,][["syllable"]]){ #Iterate through syllables
    	data_acc <- data[data[["syllable"]] == syll & as.numeric(data[["accuracy"]]) > 0.80, ] #filter by syllable and by accurate iterations
    	if(nrow(data_acc)!=0) { #if syllable is accurated learned at least once
    		lowest <- min(data_acc$"iterations") #Find the first accurate production
    		syll_acc[[length(syll_acc)+1]] <- list(lowest, syll) #add to list
    	}
    	else{ #if syllable is never accurately learned
    		syll_acc[[length(syll_acc)+1]] <- list(100000, syll) #make it very late (sloppy--- should be MAX)
    	}
    }
    syll_acc <- syll_acc[order(sapply(syll_acc,'[[',1))] #sort by lowest iteration
    syll_string = ""
    for (pair in syll_acc) #For each syllable-iteration pair
    {
    	syll_string <- paste(syll_string,pair[[2]]) #build a string of syllable orders
    }
    or_list[[length(or_list)+1]] <- syll_string #add syllable order string to order list
 }
 counts <- list()
for (i in seq(1, length(or_list))){ #iterate through order list
	if(or_list[[i]] %in% names(counts)){
		counts[or_list[[i]]] <- as.numeric(counts[or_list[[i]]]) + 1
	}
	else{
		counts[or_list[[i]]] = 1
	}
}
for (n in names(counts)) {
	print(paste(paste("Path:", n),paste(paste("taken",counts[[n]]), "times.")))
}