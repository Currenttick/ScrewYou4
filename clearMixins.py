import os
import shutil

mixinPath = r"src/main/java/fr/geming400/screwyou4/mixin"

if os.path.isdir(mixinPath):
    print("Deleting mixin folder")
    shutil.rmtree(mixinPath)

    print("Recreating mixin folder")
    os.makedirs(mixinPath)

    print("Finished !")
else:
    print("Cannot delete anything since the mixin folder doesn't exist !")