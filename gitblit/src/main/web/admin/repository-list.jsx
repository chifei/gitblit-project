import React from "react";
import {Breadcrumb, Layout, Table} from "element-react";
import {Link} from "react-router-dom";
import {library} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFolder} from "@fortawesome/free-solid-svg-icons";

library.add(faFolder);

export default class RepoList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            columns: [
                {
                    label: "",
                    width: "80px",
                    render: () => <FontAwesomeIcon icon="folder"/>
                },
                {
                    label: "Name",
                    render: data => <Link to={`/console/repo/${data.name}`}>{data.name}</Link>
                },
                {
                    label: "Date",
                    prop: "lastChange"
                }
            ],
            data: [{
                lastChange: "2019-05-02",
                name: "Project 1",
                link: "/repo/repo-1"
            }, {
                lastChange: "2019-05-04",
                name: "Project 2",
                link: "/repo/repo-2"
            }, {
                lastChange: "2019-05-01",
                name: "Project 3",
                link: "/repo/repo-3"
            }, {
                lastChange: "2019-05-03",
                name: "Project 4",
                link: "/repo/repo-4"
            }]
        };
    }

    componentDidMount() {
        fetch("/api/repository/list").then((data) => {
            this.setState({data});
        });
    }

    render() {
        return (
            <div>
                <Layout.Row>
                    <Layout.Col span="24">
                        <Breadcrumb separator="/">
                            <Breadcrumb.Item><Link to="/">Home</Link></Breadcrumb.Item>
                            <Breadcrumb.Item>Project</Breadcrumb.Item>
                        </Breadcrumb>
                    </Layout.Col>
                </Layout.Row>
                <Layout.Row>
                    <Layout.Col span="24">
                        <Table
                            style={{width: "100%"}}
                            columns={this.state.columns}
                            data={this.state.data}
                            stripe={true}
                        />
                    </Layout.Col>
                </Layout.Row>


            </div>
        );
    }

}