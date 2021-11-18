# hackathon-2021-stateful-flink-processes
Resources for making Flink processes stateful

# Releasing the docker image
Follow the instructions [here](https://github.com/apache/flink-docker/tree/dev-master).

Tag the image while building as <code>281050902442.dkr.ecr.eu-central-1.amazonaws.com/hackathon-2021-stateful-flink-process/flink:<IMAGE_TAG></code>.

Push the image as <code>docker push 281050902442.dkr.ecr.eu-central-1.amazonaws.com/hackathon-2021-stateful-flink-process/flink:<IMAGE_TAG></code>.

Do not forget to update the image properties in k8s/statefulset.yaml when trying with a new image.

If you want to run with vanilla Flink:
 - Use vanilla Flink docker image
 - Update k8s/statefulset - remove PVC and PV

# Set the default namespace
<code>kubectl config set-context --current --namespace=hackathon-2021-stateful-flink-process</code>

# Set up the permission
<code>kubectl create serviceaccount flink-high-availability</code>

<code>kubectl create role flink-high-availability --verb=create,get,update,delete,watch,list --resource=configmaps</code>

<code>kubectl create rolebinding flink-high-availability --role=flink-high-availability --serviceaccount=hackathon-2021-stateful-flink-process:flink-high-availability</code>

# Set up the services
<code>kubectl apply -f k8s/service.yaml</code>

# Set up the configuration
<code>kubectl apply -f k8s/configmap.yaml</code>

# Set up the cluster
<code>kubectl apply -f k8s/statefulset.yaml</code>